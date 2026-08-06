package com.alivos.api.dto;

import com.alivos.api.entity.AppointmentStatus;
import com.alivos.api.entity.BookingSource;
import com.alivos.api.entity.PurchaseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {
    private String id;
    private String studentName;
    private String studentEmail;
    private String professionalId;
    private String professionalName;
    private LocalDate date;
    private LocalTime time;
    private String notes;
    private AppointmentStatus status;
    private String adminNote;
    private Integer amount;
    private PurchaseStatus purchaseStatus;
    private BookingSource bookingSource;
    private Instant createdAt;
}
