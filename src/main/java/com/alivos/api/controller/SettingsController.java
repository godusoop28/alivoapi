package com.alivos.api.controller;

import com.alivos.api.dto.SettingsDto;
import com.alivos.api.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/api/settings")
    public Map<String, SettingsDto> getSettings() {
        return Map.of("settings", settingsService.getSettings());
    }

    @PatchMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, SettingsDto> updateSettings(@RequestBody SettingsDto request) {
        return Map.of("settings", settingsService.updateSettings(request));
    }
}
