package com.alivos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsDto {
    private String whatsapp;
    private String email;
    private String appointmentUrl;
    private String instagram;
    private String facebook;
    private String website;
    private String brandName;
    private Boolean advisoryEnabled;
    private Integer advisoryPrice;
    private Integer advisorySlotMinutes;
    private String advisoryDays;
    private String advisoryStartTime;
    private String advisoryEndTime;
}
