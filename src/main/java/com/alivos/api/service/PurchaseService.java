package com.alivos.api.service;

import com.alivos.api.dto.PurchaseDto;
import com.alivos.api.entity.Course;
import com.alivos.api.entity.CourseStatus;
import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.EnrollmentSource;
import com.alivos.api.entity.EnrollmentStatus;
import com.alivos.api.entity.Purchase;
import com.alivos.api.entity.PurchaseMethod;
import com.alivos.api.entity.PurchaseStatus;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.CourseRepository;
import com.alivos.api.repository.EnrollmentRepository;
import com.alivos.api.repository.PurchaseRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PurchaseDto> listPurchases() {
        return purchaseRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PurchaseDto createPurchase(String userId, String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> ApiException.notFound("Curso no encontrado"));
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw ApiException.notFound("Curso no encontrado");
        }

        boolean alreadyHasAccess = enrollmentRepository.findByUserIdAndCourseId(userId, course.getId())
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .isPresent();
        if (alreadyHasAccess) {
            throw ApiException.conflict("Ya tienes acceso a este curso");
        }

        Purchase existingPending = purchaseRepository
                .findFirstByUserIdAndCourseIdAndStatus(userId, course.getId(), PurchaseStatus.PENDING)
                .orElse(null);
        if (existingPending != null) {
            return toDto(existingPending);
        }

        Purchase purchase = new Purchase();
        purchase.setUser(userRepository.getReferenceById(userId));
        purchase.setCourse(course);
        purchase.setAmount(course.getPrice());
        purchase.setStatus(PurchaseStatus.PENDING);
        purchase.setMethod(PurchaseMethod.MERCADO_PAGO);
        purchase = purchaseRepository.save(purchase);

        return toDto(purchase);
    }

    @Transactional
    public PurchaseDto confirmPayment(String id, PurchaseStatus status) {
        if (status != PurchaseStatus.PAID && status != PurchaseStatus.FAILED) {
            throw ApiException.badRequest("El estado debe ser PAID o FAILED");
        }

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Compra no encontrada"));
        purchase.setStatus(status);
        purchase = purchaseRepository.save(purchase);

        if (status == PurchaseStatus.PAID) {
            String userId = purchase.getUser().getId();
            String courseId = purchase.getCourse().getId();
            Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                    .orElseGet(Enrollment::new);
            if (enrollment.getUser() == null) {
                enrollment.setUser(purchase.getUser());
                enrollment.setCourse(purchase.getCourse());
            }
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setSource(EnrollmentSource.PURCHASE);
            enrollmentRepository.save(enrollment);
        }

        return toDto(purchase);
    }

    private PurchaseDto toDto(Purchase p) {
        return new PurchaseDto(
                p.getId(), p.getUser().getName(), p.getUser().getEmail(), p.getCourse().getTitle(),
                p.getAmount(), p.getStatus(), p.getMethod(), p.getPaymentId(), p.getCreatedAt()
        );
    }
}
