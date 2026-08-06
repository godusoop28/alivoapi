package com.alivos.api.dto;

import com.alivos.api.entity.ManualAccessStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAccessDto {
    private String id;
    private String email;
    private Instant grantedAt;
    private String grantedBy;
    private String reason;
    private Instant expiresAt;
    private ManualAccessStatus status;
}
