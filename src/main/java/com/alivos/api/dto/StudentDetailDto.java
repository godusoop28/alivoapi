package com.alivos.api.dto;

import com.alivos.api.entity.EnrollmentSource;
import com.alivos.api.entity.EnrollmentStatus;
import com.alivos.api.entity.PurchaseMethod;
import com.alivos.api.entity.PurchaseStatus;
import com.alivos.api.entity.TaskSubmissionStatus;
import com.alivos.api.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private UserStatus status;
    private Instant createdAt;
    private List<CourseSummary> courses;
    private List<PurchaseSummary> purchases;
    private List<TaskSummary> tasks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseSummary {
        private String id;
        private String title;
        private String ageRange;
        private String imageUrl;
        private Integer progress;
        private EnrollmentStatus status;
        private EnrollmentSource source;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseSummary {
        private String id;
        private String courseTitle;
        private Integer amount;
        private PurchaseStatus status;
        private PurchaseMethod method;
        private String paymentId;
        private Instant createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskSummary {
        private String id;
        private String lessonTitle;
        private TaskSubmissionStatus status;
        private Instant deliveredAt;
    }
}
