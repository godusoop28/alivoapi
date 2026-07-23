package com.alivos.api.dto;

import com.alivos.api.entity.PurchaseMethod;
import com.alivos.api.entity.PurchaseStatus;
import com.alivos.api.entity.TaskSubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardDto {
    private Stats stats;
    private List<CourseSummary> courses;
    private List<TaskSummary> tasks;
    private List<PurchaseSummary> purchases;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long activeCourses;
        private long lessonsCompleted;
        private long lessonsTotal;
        private long pendingTasks;
        private long avgProgress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseSummary {
        private String id;
        private String slug;
        private String title;
        private String ageRange;
        private String shortDescription;
        private String imageUrl;
        private Integer progress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskSummary {
        private String id;
        private String lessonTitle;
        private String courseTitle;
        private TaskSubmissionStatus status;
        private Instant deliveredAt;
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
        private Instant createdAt;
    }
}
