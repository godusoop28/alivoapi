package com.alivos.api.controller;

import com.alivos.api.dto.AppointmentDto;
import com.alivos.api.dto.AppointmentRequest;
import com.alivos.api.security.SecurityUtils;
import com.alivos.api.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advisory")
@RequiredArgsConstructor
public class AdvisoryController {

    private final AppointmentService appointmentService;

    @GetMapping("/availability")
    public Map<String, List<String>> getAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return Map.of("slots", appointmentService.getAvailability(date));
    }

    @PostMapping("/appointments")
    public AppointmentDto createAppointment(@Valid @RequestBody AppointmentRequest request, Authentication authentication) {
        String userId = SecurityUtils.requireUserId(authentication);
        return appointmentService.createAppointment(userId, request);
    }

    @GetMapping("/appointments/me")
    public Map<String, List<AppointmentDto>> myAppointments(Authentication authentication) {
        String userId = SecurityUtils.requireUserId(authentication);
        return Map.of("appointments", appointmentService.listMyAppointments(userId));
    }
}
