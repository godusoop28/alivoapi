package com.alivos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalDto {
    private String id;
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
