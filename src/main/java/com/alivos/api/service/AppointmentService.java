package com.alivos.api.service;

import com.alivos.api.dto.AdminAppointmentRequest;
import com.alivos.api.dto.AppointmentBookingDto;
import com.alivos.api.dto.AppointmentDto;
import com.alivos.api.dto.AppointmentRequest;
import com.alivos.api.entity.Appointment;
import com.alivos.api.entity.AppointmentStatus;
import com.alivos.api.entity.BookingSource;
import com.alivos.api.entity.ManualAccessStatus;
import com.alivos.api.entity.Professional;
import com.alivos.api.entity.Purchase;
import com.alivos.api.entity.PurchaseStatus;
import com.alivos.api.entity.Settings;
import com.alivos.api.entity.User;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.AppointmentAccessRepository;
import com.alivos.api.repository.AppointmentRepository;
import com.alivos.api.repository.ProfessionalRepository;
import com.alivos.api.repository.PurchaseRepository;
import com.alivos.api.repository.SettingsRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;
    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final AppointmentAccessRepository appointmentAccessRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseService purchaseService;

    @Transactional(readOnly = true)
    public List<String> getAvailability(String professionalId, LocalDate date) {
        if (date == null || date.isBefore(LocalDate.now())) {
            return List.of();
        }
        Settings settings = settingsRepository.findById(Settings.SINGLETON_ID).orElse(null);
        if (settings != null && Boolean.FALSE.equals(settings.getAdvisoryEnabled())) {
            return List.of();
        }

        Professional professional = professionalRepository.findById(professionalId)
                .filter(Professional::getActive)
                .orElseThrow(() -> ApiException.notFound("Profesional no encontrado"));

        Set<Integer> days = parseDays(professional.getWorkDays());
        if (!days.contains(date.getDayOfWeek().getValue())) {
            return List.of();
        }

        LocalTime start = parseTime(professional.getStartTime(), LocalTime.of(9, 0));
        LocalTime end = parseTime(professional.getEndTime(), LocalTime.of(18, 0));
        int slotMinutes = professional.getSlotMinutes() != null ? professional.getSlotMinutes() : 30;

        Set<LocalTime> taken = appointmentRepository
                .findByProfessionalIdAndDateAndStatusIn(professionalId, date, ACTIVE_STATUSES).stream()
                .map(Appointment::getTime)
                .collect(Collectors.toSet());

        boolean isToday = date.isEqual(LocalDate.now());
        LocalTime now = LocalTime.now();

        List<String> slots = new ArrayList<>();
        LocalTime cursor = start;
        while (!cursor.plusMinutes(slotMinutes).isAfter(end)) {
            boolean isPast = isToday && !cursor.isAfter(now);
            if (!isPast && !taken.contains(cursor)) {
                slots.add(cursor.toString());
            }
            cursor = cursor.plusMinutes(slotMinutes);
        }
        return slots;
    }

    @Transactional
    public AppointmentBookingDto createAppointment(String userId, AppointmentRequest input) {
        List<String> available = getAvailability(input.getProfessionalId(), input.getDate());
        if (!available.contains(input.getTime().toString())) {
            throw ApiException.conflict("Ese horario ya no está disponible, elige otro.");
        }

        Professional professional = professionalRepository.findById(input.getProfessionalId())
                .orElseThrow(() -> ApiException.notFound("Profesional no encontrado"));
        User user = userRepository.getReferenceById(userId);

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setProfessional(professional);
        appointment.setDate(input.getDate());
        appointment.setTime(input.getTime());
        appointment.setNotes(input.getNotes());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setBookingSource(BookingSource.USER);

        Integer price = effectiveAdvisoryPrice();
        boolean hasFreeAccess = hasActiveFreeAccess(userId);

        if (!hasFreeAccess && price > 0) {
            appointment.setAmount(price);
            appointment = appointmentRepository.save(appointment);
            var purchase = purchaseService.createForAppointment(user, appointment, price);
            return new AppointmentBookingDto(toDto(appointment), purchase, true);
        }

        appointment = appointmentRepository.save(appointment);
        return new AppointmentBookingDto(toDto(appointment), null, false);
    }

    @Transactional
    public AppointmentDto createByAdmin(AdminAppointmentRequest input) {
        String email = input.getUserEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("No existe un usuario con ese correo. Pídele que se registre primero."));
        Professional professional = professionalRepository.findById(input.getProfessionalId())
                .orElseThrow(() -> ApiException.notFound("Profesional no encontrado"));

        boolean taken = appointmentRepository
                .findByProfessionalIdAndDateAndStatusIn(professional.getId(), input.getDate(), ACTIVE_STATUSES).stream()
                .anyMatch(a -> a.getTime().equals(input.getTime()));
        if (taken) {
            throw ApiException.conflict("Ese profesional ya tiene una cita en ese horario.");
        }

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setProfessional(professional);
        appointment.setDate(input.getDate());
        appointment.setTime(input.getTime());
        appointment.setNotes(input.getNotes());
        appointment.setStatus(input.getStatus() != null ? input.getStatus() : AppointmentStatus.CONFIRMED);
        appointment.setBookingSource(BookingSource.ADMIN);
        appointment = appointmentRepository.save(appointment);

        return toDto(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> listMyAppointments(String userId) {
        return appointmentRepository.findByUserIdOrderByDateDescTimeDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> listAll() {
        return appointmentRepository.findAllByOrderByDateDescTimeDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AppointmentDto updateStatus(String id, AppointmentStatus status, String adminNote) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Cita no encontrada"));

        if (status == AppointmentStatus.CONFIRMED && appointment.getAmount() != null) {
            Purchase purchase = purchaseRepository.findFirstByAppointmentIdOrderByCreatedAtDesc(id).orElse(null);
            if (purchase == null || purchase.getStatus() != PurchaseStatus.PAID) {
                throw ApiException.conflict("Esta cita todavía no está pagada, no se puede confirmar.");
            }
        }

        appointment.setStatus(status);
        if (adminNote != null) appointment.setAdminNote(adminNote);
        appointment = appointmentRepository.save(appointment);
        return toDto(appointment);
    }

    @Transactional
    public AppointmentDto reschedule(String id, LocalDate date, LocalTime time) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Cita no encontrada"));
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment = appointmentRepository.save(appointment);
        return toDto(appointment);
    }

    private Integer effectiveAdvisoryPrice() {
        Settings settings = settingsRepository.findById(Settings.SINGLETON_ID).orElse(null);
        Integer price = settings != null ? settings.getAdvisoryPrice() : null;
        return price != null && price > 0 ? price : 0;
    }

    private boolean hasActiveFreeAccess(String userId) {
        return appointmentAccessRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, ManualAccessStatus.ACTIVE)
                .filter(access -> access.getExpiresAt() == null || access.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    private Set<Integer> parseDays(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of(1, 2, 3, 4, 5);
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    private LocalTime parseTime(String raw, LocalTime fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return LocalTime.parse(raw);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private AppointmentDto toDto(Appointment a) {
        PurchaseStatus purchaseStatus = a.getAmount() == null ? null : purchaseRepository
                .findFirstByAppointmentIdOrderByCreatedAtDesc(a.getId())
                .map(Purchase::getStatus)
                .orElse(null);
        return new AppointmentDto(
                a.getId(),
                a.getUser().getName(),
                a.getUser().getEmail(),
                a.getProfessional() != null ? a.getProfessional().getId() : null,
                a.getProfessional() != null ? a.getProfessional().getName() : null,
                a.getDate(),
                a.getTime(),
                a.getNotes(),
                a.getStatus(),
                a.getAdminNote(),
                a.getAmount(),
                purchaseStatus,
                a.getBookingSource(),
                a.getCreatedAt()
        );
    }
}
