package com.alivos.api.service;

import com.alivos.api.dto.PurchaseDto;
import com.alivos.api.entity.Appointment;
import com.alivos.api.entity.Course;
import com.alivos.api.entity.CourseStatus;
import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.EnrollmentSource;
import com.alivos.api.entity.EnrollmentStatus;
import com.alivos.api.entity.Purchase;
import com.alivos.api.entity.PurchaseMethod;
import com.alivos.api.entity.PurchaseStatus;
import com.alivos.api.entity.PurchaseType;
import com.alivos.api.entity.User;
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
    private final MercadoPagoService mercadoPagoService;

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

        Purchase purchase = purchaseRepository
                .findFirstByUserIdAndCourseIdAndStatus(userId, course.getId(), PurchaseStatus.PENDING)
                .orElseGet(Purchase::new);
        if (purchase.getUser() == null) {
            purchase.setUser(userRepository.getReferenceById(userId));
            purchase.setCourse(course);
            purchase.setType(PurchaseType.COURSE);
            purchase.setStatus(PurchaseStatus.PENDING);
            purchase.setMethod(PurchaseMethod.MERCADO_PAGO);
        }
        purchase.setAmount(course.getPrice());
        purchase = purchaseRepository.save(purchase);

        var preference = mercadoPagoService.createPreference(purchase.getId(), course.getTitle(), course.getPrice());
        purchase.setPreferenceId(preference.preferenceId());
        purchase = purchaseRepository.save(purchase);

        return toDto(purchase, preference.initPoint());
    }

    /**
     * Called by AppointmentService when a user books without free access —
     * creates the pending Purchase + Mercado Pago preference for the
     * appointment's flat advisory price.
     */
    @Transactional
    public PurchaseDto createForAppointment(User user, Appointment appointment, Integer amount) {
        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setAppointment(appointment);
        purchase.setType(PurchaseType.APPOINTMENT);
        purchase.setAmount(amount);
        purchase.setStatus(PurchaseStatus.PENDING);
        purchase.setMethod(PurchaseMethod.MERCADO_PAGO);
        purchase = purchaseRepository.save(purchase);

        String title = "Cita de asesoramiento ALIVOS - " + appointment.getDate() + " " + appointment.getTime();
        var preference = mercadoPagoService.createPreference(purchase.getId(), title, amount);
        purchase.setPreferenceId(preference.preferenceId());
        purchase = purchaseRepository.save(purchase);

        return toDto(purchase, preference.initPoint());
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

        if (status == PurchaseStatus.PAID && purchase.getType() == PurchaseType.COURSE) {
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
        // For PurchaseType.APPOINTMENT, paying only marks the Purchase as
        // PAID — the appointment itself still needs the admin's approval.

        return toDto(purchase);
    }

    private PurchaseDto toDto(Purchase p) {
        return toDto(p, null);
    }

    private PurchaseDto toDto(Purchase p, String initPoint) {
        String label = p.getType() == PurchaseType.APPOINTMENT
                ? "Cita de asesoramiento (" + p.getAppointment().getDate() + " " + p.getAppointment().getTime() + ")"
                : p.getCourse().getTitle();
        return new PurchaseDto(
                p.getId(), p.getUser().getName(), p.getUser().getEmail(), label,
                p.getType(), p.getAmount(), p.getStatus(), p.getMethod(), p.getPaymentId(), p.getCreatedAt(),
                initPoint
        );
    }
}
