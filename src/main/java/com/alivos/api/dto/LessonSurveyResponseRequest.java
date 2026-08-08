package com.alivos.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LessonSurveyResponseRequest {
    @NotBlank(message = "Las respuestas son obligatorias")
    private String answers;
}
