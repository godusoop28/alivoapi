package com.alivos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class VimeoUploadTicketRequest {
    @NotBlank(message = "El nombre del archivo es obligatorio")
    private String fileName;

    @NotNull(message = "El tamaño del archivo es obligatorio")
    @Positive(message = "El tamaño del archivo debe ser mayor a 0")
    private Long fileSizeBytes;
}
