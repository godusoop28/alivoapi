package com.alivos.api.service;

import com.alivos.api.dto.AppointmentAccessDto;
import com.alivos.api.dto.AppointmentAccessRequest;
import com.alivos.api.entity.AppointmentAccess;
import com.alivos.api.entity.ManualAccessStatus;
import com.alivos.api.entity.Role;
import com.alivos.api.entity.User;
import com.alivos.api.entity.UserStatus;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.AppointmentAccessRepository;
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
public class AppointmentAccessService {

    /** Same demo behavior as ManualAccessService: auto-create the account if it doesn't exist yet. */
    private static final String TEMP_PASSWORD = "Alivos12345!";

    private final AppointmentAccessRepository appointmentAccessRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AppointmentAccessDto> listAccesses() {
        return appointmentAccessRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public AppointmentAccessDto grantAccess(String grantedById, AppointmentAccessRequest input) {
        String email = input.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User created = new User();
            created.setName(email.split("@")[0]);
            created.setEmail(email);
            created.setPasswordHash(passwordEncoder.encode(TEMP_PASSWORD));
            created.setRole(Role.STUDENT);
            created.setStatus(UserStatus.ACTIVE);
            return userRepository.save(created);
        });

        AppointmentAccess access = new AppointmentAccess();
        access.setEmail(email);
        access.setUser(user);
        access.setGrantedBy(userRepository.getReferenceById(grantedById));
        access.setReason(input.getReason());
        access.setExpiresAt(parseExpiresAt(input.getExpiresAt()));
        access.setStatus(ManualAccessStatus.ACTIVE);
        access = appointmentAccessRepository.save(access);

        return toDto(access);
    }

    @Transactional
    public void revokeAccess(String id) {
        AppointmentAccess access = appointmentAccessRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Acceso no encontrado"));
        access.setStatus(ManualAccessStatus.REVOKED);
        appointmentAccessRepository.save(access);
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

    private AppointmentAccessDto toDto(AppointmentAccess access) {
        return new AppointmentAccessDto(
                access.getId(),
                access.getEmail(),
                access.getCreatedAt(),
                access.getGrantedBy().getName(),
                access.getReason(),
                access.getExpiresAt(),
                access.getStatus()
        );
    }
}
