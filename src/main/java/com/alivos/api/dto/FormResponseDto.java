package com.alivos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormResponseDto {
    private String id;
    private String studentName;
    private String studentEmail;
    private String answers;
    private Instant submittedAt;
}
