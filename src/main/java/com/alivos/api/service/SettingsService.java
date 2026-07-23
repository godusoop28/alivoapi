package com.alivos.api.service;

import com.alivos.api.dto.SettingsDto;
import com.alivos.api.entity.Settings;
import com.alivos.api.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;

    @Transactional(readOnly = true)
    public SettingsDto getSettings() {
        Settings settings = settingsRepository.findById(Settings.SINGLETON_ID).orElse(null);
        if (settings == null) {
            return new SettingsDto(null, null, null, null, null, null, "ALIVOS Medicina de Rehabilitación");
        }
        return toDto(settings);
    }

    @Transactional
    public SettingsDto updateSettings(SettingsDto input) {
        Settings settings = settingsRepository.findById(Settings.SINGLETON_ID).orElseGet(() -> {
            Settings created = new Settings();
            created.setId(Settings.SINGLETON_ID);
            return created;
        });

        if (input.getWhatsapp() != null) settings.setWhatsapp(input.getWhatsapp());
        if (input.getEmail() != null) settings.setEmail(input.getEmail());
        if (input.getAppointmentUrl() != null) settings.setAppointmentUrl(input.getAppointmentUrl());
        if (input.getInstagram() != null) settings.setInstagram(input.getInstagram());
        if (input.getFacebook() != null) settings.setFacebook(input.getFacebook());
        if (input.getWebsite() != null) settings.setWebsite(input.getWebsite());
        if (input.getBrandName() != null) settings.setBrandName(input.getBrandName());

        settings = settingsRepository.save(settings);
        return toDto(settings);
    }

    private SettingsDto toDto(Settings settings) {
        return new SettingsDto(
                settings.getWhatsapp(), settings.getEmail(), settings.getAppointmentUrl(),
                settings.getInstagram(), settings.getFacebook(), settings.getWebsite(), settings.getBrandName()
        );
    }
}
