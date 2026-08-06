package com.alivos.api.dto;

import com.alivos.api.entity.CourseReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseReviewStatusRequest {
    @NotNull(message = "El estado es obligatorio")
    private CourseReviewStatus status;
}
