package com.alivos.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ManualAccessRequest {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    private String email;

    @NotBlank(message = "El curso es obligatorio")
    private String courseId;

    private String reason;
    private String expiresAt;
}
