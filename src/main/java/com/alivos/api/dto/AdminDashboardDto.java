package com.alivos.api.dto;

import com.alivos.api.entity.CourseStatus;
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
public class AdminDashboardDto {
    private Stats stats;
    private List<PurchaseSummary> recentPurchases;
    private List<TaskSummary> pendingTasks;
    private List<CourseSummary> courses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long totalStudents;
        private long publishedCourses;
        private long monthlyRevenue;
        private long pendingTasks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseSummary {
        private String id;
        private String studentName;
        private String courseTitle;
        private Integer amount;
        private PurchaseStatus status;
        private Instant createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskSummary {
        private String id;
        private String studentName;
        private String lessonTitle;
        private TaskSubmissionStatus status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseSummary {
        private String id;
        private String title;
        private String ageRange;
        private String imageUrl;
        private CourseStatus status;
        private long studentsCount;
        private long lessonsCount;
    }
}
