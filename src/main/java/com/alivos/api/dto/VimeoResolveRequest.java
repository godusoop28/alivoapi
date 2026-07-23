package com.alivos.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VimeoResolveRequest {
    @NotBlank(message = "La URL es obligatoria")
    private String url;
}
