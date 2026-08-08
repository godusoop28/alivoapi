package com.alivos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageDto {
    private String id;
    private String name;
    private String lastName;
    private String email;
    private String message;
    private Boolean read;
    private Instant createdAt;
}
