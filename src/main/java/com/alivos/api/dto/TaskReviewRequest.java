package com.alivos.api.dto;

import com.alivos.api.entity.TaskSubmissionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskReviewRequest {
    @NotNull(message = "El estado es obligatorio")
    private TaskSubmissionStatus status;
    private String adminComment;
}
