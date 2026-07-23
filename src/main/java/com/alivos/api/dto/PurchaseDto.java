package com.alivos.api.dto;

import com.alivos.api.entity.PurchaseMethod;
import com.alivos.api.entity.PurchaseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDto {
    private String id;
    private String studentName;
    private String studentEmail;
    private String courseTitle;
    private Integer amount;
    private PurchaseStatus status;
    private PurchaseMethod method;
    private String paymentId;
    private Instant createdAt;
}
