package com.alivos.api.dto;

import com.alivos.api.entity.TaskSubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmissionDto {
    private String id;
    private String studentName;
    private String studentEmail;
    private String lessonTitle;
    private String courseTitle;
    private String taskInstructions;
    private String studentAnswer;
    private String fileUrl;
    private String lessonPdfUrl;
    private String lessonImageUrl;
    private TaskSubmissionStatus status;
    private String adminComment;
    private Instant deliveredAt;
    private Instant reviewedAt;
}
