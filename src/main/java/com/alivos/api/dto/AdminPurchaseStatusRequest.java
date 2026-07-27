package com.alivos.api.dto;

import com.alivos.api.entity.PurchaseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminPurchaseStatusRequest {
    @NotNull(message = "El estado es obligatorio")
    private PurchaseStatus status;
}
