package com.alivos.api.dto;

import com.alivos.api.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentStatusRequest {
    @NotNull(message = "El estado es obligatorio")
    private AppointmentStatus status;

    private String adminNote;
}
