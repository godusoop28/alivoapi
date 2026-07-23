package com.alivos.api.service;

import com.alivos.api.dto.StudentDashboardDto;
import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.EnrollmentStatus;
import com.alivos.api.entity.Purchase;
import com.alivos.api.entity.TaskSubmission;
import com.alivos.api.entity.TaskSubmissionStatus;
import com.alivos.api.repository.EnrollmentRepository;
import com.alivos.api.repository.LessonProgressRepository;
import com.alivos.api.repository.LessonRepository;
import com.alivos.api.repository.PurchaseRepository;
import com.alivos.api.repository.TaskSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional(readOnly = true)
    public StudentDashboardDto getDashboard(String userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(userId, EnrollmentStatus.ACTIVE);

        long lessonsTotal = enrollments.stream()
                .mapToLong(e -> lessonRepository.countVisibleByCourseId(e.getCourse().getId()))
                .sum();
        long lessonsCompleted = lessonProgressRepository.countByUserIdAndCompletedTrue(userId);

        List<TaskSubmission> tasks = taskSubmissionRepository.findByUserIdOrderByDeliveredAtDesc(userId)
                .stream().limit(5).toList();
        List<Purchase> purchases = purchaseRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().limit(5).toList();

        long pendingTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskSubmissionStatus.DELIVERED || t.getStatus() == TaskSubmissionStatus.PENDING)
                .count();

        long avgProgress = enrollments.isEmpty() ? 0 : Math.round(
                enrollments.stream().mapToInt(Enrollment::getProgress).average().orElse(0)
        );

        StudentDashboardDto.Stats stats = new StudentDashboardDto.Stats(
                enrollments.size(), lessonsCompleted, lessonsTotal, pendingTasks, avgProgress
        );

        List<StudentDashboardDto.CourseSummary> courses = enrollments.stream()
                .map(e -> new StudentDashboardDto.CourseSummary(
                        e.getCourse().getId(),
                        e.getCourse().getSlug(),
                        e.getCourse().getTitle(),
                        e.getCourse().getAgeRange(),
                        e.getCourse().getShortDescription(),
                        e.getCourse().getCoverImage(),
                        e.getProgress()
                )).toList();

        List<StudentDashboardDto.TaskSummary> taskSummaries = tasks.stream()
                .map(t -> new StudentDashboardDto.TaskSummary(
                        t.getId(),
                        t.getLesson().getTitle(),
                        t.getLesson().getModule().getCourse().getTitle(),
                        t.getStatus(),
                        t.getDeliveredAt()
                )).toList();

        List<StudentDashboardDto.PurchaseSummary> purchaseSummaries = purchases.stream()
                .map(p -> new StudentDashboardDto.PurchaseSummary(
                        p.getId(),
                        p.getCourse().getTitle(),
                        p.getAmount(),
                        p.getStatus(),
                        p.getMethod(),
                        p.getCreatedAt()
                )).toList();

        return new StudentDashboardDto(stats, courses, taskSummaries, purchaseSummaries);
    }
}
