package com.alivos.api.dto;

import com.alivos.api.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {
    private String id;
    private String studentName;
    private String studentEmail;
    private LocalDate date;
    private LocalTime time;
    private String notes;
    private AppointmentStatus status;
    private String adminNote;
    private Instant createdAt;
}
