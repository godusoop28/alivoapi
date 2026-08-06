package com.alivos.api.dto;

import lombok.Data;

@Data
public class ProfessionalRequest {
    private String name;
    private String title;
    private String bio;
    private String photoUrl;
    private Boolean active;
    private Integer slotMinutes;
    private String workDays;
    private String startTime;
    private String endTime;
}
