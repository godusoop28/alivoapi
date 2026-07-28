package com.alivos.api.controller;

import com.alivos.api.dto.AppointmentDto;
import com.alivos.api.dto.AppointmentRescheduleRequest;
import com.alivos.api.dto.AppointmentStatusRequest;
import com.alivos.api.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public Map<String, List<AppointmentDto>> listAppointments() {
        return Map.of("appointments", appointmentService.listAll());
    }

    @PatchMapping("/{id}/status")
    public AppointmentDto updateStatus(@PathVariable String id, @Valid @RequestBody AppointmentStatusRequest request) {
        return appointmentService.updateStatus(id, request.getStatus(), request.getAdminNote());
    }

    @PatchMapping("/{id}/reschedule")
    public AppointmentDto reschedule(@PathVariable String id, @Valid @RequestBody AppointmentRescheduleRequest request) {
        return appointmentService.reschedule(id, request.getDate(), request.getTime());
    }
}
