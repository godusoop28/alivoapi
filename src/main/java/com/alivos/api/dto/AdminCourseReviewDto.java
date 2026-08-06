package com.alivos.api.dto;

import com.alivos.api.entity.CourseReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseReviewDto {
    private String id;
    private String courseTitle;
    private String studentName;
    private Integer rating;
    private String comment;
    private CourseReviewStatus status;
    private Instant createdAt;
}
