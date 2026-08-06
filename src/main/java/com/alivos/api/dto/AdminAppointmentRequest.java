package com.alivos.api.dto;

import com.alivos.api.entity.AppointmentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AdminAppointmentRequest {
    @NotBlank(message = "El correo del usuario es obligatorio")
    @Email(message = "Correo inválido")
    private String userEmail;

    @NotBlank(message = "El profesional es obligatorio")
    private String professionalId;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime time;

    private String notes;

    private AppointmentStatus status;
}
