package com.alivos.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactMessageRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String lastName;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    private String email;

    @NotBlank(message = "El mensaje es obligatorio")
    private String message;
}
