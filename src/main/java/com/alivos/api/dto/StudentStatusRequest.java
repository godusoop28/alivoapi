package com.alivos.api.dto;

import com.alivos.api.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentStatusRequest {
    @NotNull(message = "El estado es obligatorio")
    private UserStatus status;
}
