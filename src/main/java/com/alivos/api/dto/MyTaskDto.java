package com.alivos.api.dto;

import com.alivos.api.entity.TaskSubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyTaskDto {
    private String taskInstructions;
    private String lessonPdfUrl;
    private String lessonImageUrl;
    private TaskSubmissionStatus status;
    private List<TaskCommentDto> comments;
    private Instant deliveredAt;
    private Instant reviewedAt;
}
