package com.alivos.api.controller;

import com.alivos.api.dto.ManualAccessDto;
import com.alivos.api.dto.ManualAccessRequest;
import com.alivos.api.security.SecurityUtils;
import com.alivos.api.service.ManualAccessService;
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
@RequestMapping("/api/admin/manual-access")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ManualAccessController {

    private final ManualAccessService manualAccessService;

    @GetMapping
    public Map<String, List<ManualAccessDto>> listAccesses() {
        return Map.of("accesses", manualAccessService.listAccesses());
    }

    @PostMapping
    public Map<String, ManualAccessDto> grantAccess(
            @Valid @RequestBody ManualAccessRequest request,
            Authentication authentication
    ) {
        String grantedById = SecurityUtils.requireUserId(authentication);
        return Map.of("access", manualAccessService.grantAccess(grantedById, request));
    }

    @PatchMapping("/{id}/revoke")
    public Map<String, Boolean> revokeAccess(@PathVariable String id) {
        manualAccessService.revokeAccess(id);
        return Map.of("ok", true);
    }
}
