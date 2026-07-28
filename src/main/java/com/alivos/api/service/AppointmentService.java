package com.alivos.api.service;

import com.alivos.api.dto.AppointmentDto;
import com.alivos.api.dto.AppointmentRequest;
import com.alivos.api.dto.AppointmentRescheduleRequest;
import com.alivos.api.entity.Appointment;
import com.alivos.api.entity.AppointmentStatus;
import com.alivos.api.entity.Settings;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.AppointmentRepository;
import com.alivos.api.repository.SettingsRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<String> getAvailability(LocalDate date) {
        if (date == null || date.isBefore(LocalDate.now())) {
            return List.of();
        }
        Settings settings = settingsRepository.findById(Settings.SINGLETON_ID).orElse(null);
        if (settings != null && Boolean.FALSE.equals(settings.getAdvisoryEnabled())) {
            return List.of();
        }

        Set<Integer> days = parseDays(settings != null ? settings.getAdvisoryDays() : null);
        if (!days.contains(date.getDayOfWeek().getValue())) {
            return List.of();
        }

        LocalTime start = parseTime(settings != null ? settings.getAdvisoryStartTime() : null, LocalTime.of(9, 0));
        LocalTime end = parseTime(settings != null ? settings.getAdvisoryEndTime() : null, LocalTime.of(18, 0));
        int slotMinutes = settings != null && settings.getAdvisorySlotMinutes() != null
                ? settings.getAdvisorySlotMinutes() : 30;

        Set<LocalTime> taken = appointmentRepository.findByDateAndStatusIn(date, ACTIVE_STATUSES).stream()
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
    public AppointmentDto createAppointment(String userId, AppointmentRequest input) {
        List<String> available = getAvailability(input.getDate());
        if (!available.contains(input.getTime().toString())) {
            throw ApiException.conflict("Ese horario ya no está disponible, elige otro.");
        }

        Appointment appointment = new Appointment();
        appointment.setUser(userRepository.getReferenceById(userId));
        appointment.setDate(input.getDate());
        appointment.setTime(input.getTime());
        appointment.setNotes(input.getNotes());
        appointment.setStatus(AppointmentStatus.PENDING);
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
        return new AppointmentDto(
                a.getId(),
                a.getUser().getName(),
                a.getUser().getEmail(),
                a.getDate(),
                a.getTime(),
                a.getNotes(),
                a.getStatus(),
                a.getAdminNote(),
                a.getCreatedAt()
        );
    }
}
