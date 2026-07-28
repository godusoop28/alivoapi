package com.alivos.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentRequest {
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime time;

    private String notes;
}
