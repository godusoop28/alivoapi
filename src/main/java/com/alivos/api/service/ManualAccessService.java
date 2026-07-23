package com.alivos.api.service;

import com.alivos.api.dto.ManualAccessDto;
import com.alivos.api.dto.ManualAccessRequest;
import com.alivos.api.entity.Course;
import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.EnrollmentSource;
import com.alivos.api.entity.EnrollmentStatus;
import com.alivos.api.entity.ManualAccess;
import com.alivos.api.entity.ManualAccessStatus;
import com.alivos.api.entity.Role;
import com.alivos.api.entity.User;
import com.alivos.api.entity.UserStatus;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.CourseRepository;
import com.alivos.api.repository.EnrollmentRepository;
import com.alivos.api.repository.ManualAccessRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualAccessService {

    /**
     * Demo behavior: if the granted email doesn't match an existing user, we
     * create a STUDENT account on the fly with this shared temp password so
     * the manual-access flow never blocks on "user must sign up first".
     */
    private static final String TEMP_PASSWORD = "Alivos12345!";

    private final ManualAccessRepository manualAccessRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<ManualAccessDto> listAccesses() {
        return manualAccessRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ManualAccessDto grantAccess(String grantedById, ManualAccessRequest input) {
        String email = input.getEmail().toLowerCase().trim();

        Course course = courseRepository.findById(input.getCourseId())
                .orElseThrow(() -> ApiException.notFound("Curso no encontrado"));

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User created = new User();
            created.setName(email.split("@")[0]);
            created.setEmail(email);
            created.setPasswordHash(passwordEncoder.encode(TEMP_PASSWORD));
            created.setRole(Role.STUDENT);
            created.setStatus(UserStatus.ACTIVE);
            return userRepository.save(created);
        });

        Instant expiresAt = parseExpiresAt(input.getExpiresAt());

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId())
                .orElseGet(Enrollment::new);
        if (enrollment.getUser() == null) {
            enrollment.setUser(user);
            enrollment.setCourse(course);
        }
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setSource(EnrollmentSource.MANUAL);
        enrollment.setExpiresAt(expiresAt);
        enrollmentRepository.save(enrollment);

        ManualAccess access = new ManualAccess();
        access.setEmail(email);
        access.setUser(user);
        access.setCourse(course);
        access.setGrantedBy(userRepository.getReferenceById(grantedById));
        access.setReason(input.getReason());
        access.setExpiresAt(expiresAt);
        access.setStatus(ManualAccessStatus.ACTIVE);
        access = manualAccessRepository.save(access);

        return toDto(access);
    }

    @Transactional
    public void revokeAccess(String id) {
        ManualAccess access = manualAccessRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Acceso manual no encontrado"));
        access.setStatus(ManualAccessStatus.REVOKED);
        manualAccessRepository.save(access);

        if (access.getUser() != null) {
            enrollmentRepository.findByUserIdAndCourseId(access.getUser().getId(), access.getCourse().getId())
                    .filter(e -> e.getSource() == EnrollmentSource.MANUAL)
                    .ifPresent(e -> {
                        e.setStatus(EnrollmentStatus.REVOKED);
                        enrollmentRepository.save(e);
                    });
        }
    }

    private Instant parseExpiresAt(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Instant.parse(raw);
        } catch (DateTimeParseException ex) {
            try {
                return java.time.LocalDate.parse(raw).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
            } catch (DateTimeParseException ex2) {
                throw ApiException.badRequest("Fecha de expiración inválida");
            }
        }
    }

    private ManualAccessDto toDto(ManualAccess access) {
        String courseTitle = access.getCourse().getTitle() + " (" + access.getCourse().getAgeRange() + ")";
        return new ManualAccessDto(
                access.getId(),
                access.getEmail(),
                courseTitle,
                access.getCreatedAt(),
                access.getGrantedBy().getName(),
                access.getReason(),
                access.getExpiresAt(),
                access.getStatus()
        );
    }
}
