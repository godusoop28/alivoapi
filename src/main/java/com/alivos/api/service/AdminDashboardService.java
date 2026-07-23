package com.alivos.api.service;

import com.alivos.api.dto.AdminDashboardDto;
import com.alivos.api.entity.Course;
import com.alivos.api.entity.CourseModule;
import com.alivos.api.entity.CourseStatus;
import com.alivos.api.entity.EnrollmentStatus;
import com.alivos.api.entity.Purchase;
import com.alivos.api.entity.PurchaseStatus;
import com.alivos.api.entity.Role;
import com.alivos.api.entity.TaskSubmission;
import com.alivos.api.entity.TaskSubmissionStatus;
import com.alivos.api.repository.CourseModuleRepository;
import com.alivos.api.repository.CourseRepository;
import com.alivos.api.repository.EnrollmentRepository;
import com.alivos.api.repository.LessonRepository;
import com.alivos.api.repository.PurchaseRepository;
import com.alivos.api.repository.TaskSubmissionRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PurchaseRepository purchaseRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboard() {
        long totalStudents = userRepository.findByRoleOrderByCreatedAtDesc(Role.STUDENT).size();
        long publishedCourses = courseRepository.findByStatusOrderByCreatedAtAsc(CourseStatus.PUBLISHED).size();

        List<Purchase> paidPurchases = purchaseRepository.findByStatusOrderByCreatedAtDesc(PurchaseStatus.PAID);
        List<TaskSubmission> pendingTasks = taskSubmissionRepository.findByStatusInOrderByDeliveredAtDesc(
                List.of(TaskSubmissionStatus.PENDING, TaskSubmissionStatus.DELIVERED)
        ).stream().limit(10).toList();

        YearMonth currentMonth = YearMonth.now(ZoneOffset.UTC);
        long monthlyRevenue = paidPurchases.stream()
                .filter(p -> YearMonth.from(p.getCreatedAt().atZone(ZoneOffset.UTC)).equals(currentMonth))
                .mapToLong(Purchase::getAmount)
                .sum();

        AdminDashboardDto.Stats stats = new AdminDashboardDto.Stats(
                totalStudents, publishedCourses, monthlyRevenue, pendingTasks.size()
        );

        List<AdminDashboardDto.PurchaseSummary> recentPurchases = paidPurchases.stream().limit(5)
                .map(p -> new AdminDashboardDto.PurchaseSummary(
                        p.getId(), p.getUser().getName(), p.getCourse().getTitle(),
                        p.getAmount(), p.getStatus(), p.getCreatedAt()
                )).toList();

        List<AdminDashboardDto.TaskSummary> taskSummaries = pendingTasks.stream()
                .map(t -> new AdminDashboardDto.TaskSummary(
                        t.getId(), t.getUser().getName(), t.getLesson().getTitle(), t.getStatus()
                )).toList();

        List<Course> allCourses = courseRepository.findAll();
        List<AdminDashboardDto.CourseSummary> courseSummaries = allCourses.stream()
                .map(c -> {
                    long studentsCount = enrollmentRepository.countByCourseIdAndStatus(c.getId(), EnrollmentStatus.ACTIVE);
                    long lessonsCount = moduleRepository.findByCourseIdOrderByOrderIndexAsc(c.getId()).stream()
                            .mapToLong(m -> lessonRepository.countByModuleId(m.getId()))
                            .sum();
                    return new AdminDashboardDto.CourseSummary(
                            c.getId(), c.getTitle(), c.getAgeRange(), c.getCoverImage(),
                            c.getStatus(), studentsCount, lessonsCount
                    );
                }).toList();

        return new AdminDashboardDto(stats, recentPurchases, taskSummaries, courseSummaries);
    }
}
