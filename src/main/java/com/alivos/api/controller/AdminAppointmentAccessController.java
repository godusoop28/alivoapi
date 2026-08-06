package com.alivos.api.controller;

import com.alivos.api.dto.AppointmentAccessDto;
import com.alivos.api.dto.AppointmentAccessRequest;
import com.alivos.api.security.SecurityUtils;
import com.alivos.api.service.AppointmentAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/appointment-access")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAppointmentAccessController {

    private final AppointmentAccessService appointmentAccessService;

    @GetMapping
    public Map<String, List<AppointmentAccessDto>> listAccesses() {
        return Map.of("accesses", appointmentAccessService.listAccesses());
    }

    @PostMapping
    public Map<String, AppointmentAccessDto> grantAccess(
            @Valid @RequestBody AppointmentAccessRequest request,
            Authentication authentication
    ) {
        String grantedById = SecurityUtils.requireUserId(authentication);
        return Map.of("access", appointmentAccessService.grantAccess(grantedById, request));
    }

    @PatchMapping("/{id}/revoke")
    public Map<String, Boolean> revokeAccess(@PathVariable String id) {
        appointmentAccessService.revokeAccess(id);
        return Map.of("ok", true);
    }
}
