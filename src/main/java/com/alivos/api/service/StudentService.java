package com.alivos.api.service;

import com.alivos.api.dto.StudentDetailDto;
import com.alivos.api.dto.StudentDto;
import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.EnrollmentStatus;
import com.alivos.api.entity.Purchase;
import com.alivos.api.entity.Role;
import com.alivos.api.entity.TaskSubmission;
import com.alivos.api.entity.User;
import com.alivos.api.entity.UserStatus;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.EnrollmentRepository;
import com.alivos.api.repository.PurchaseRepository;
import com.alivos.api.repository.TaskSubmissionRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PurchaseRepository purchaseRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;

    @Transactional(readOnly = true)
    public List<StudentDto> listStudents() {
        List<User> students = userRepository.findByRoleOrderByCreatedAtDesc(Role.STUDENT);

        return students.stream().map(student -> {
            List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(student.getId(), EnrollmentStatus.ACTIVE);
            long avgProgress = enrollments.isEmpty() ? 0 : Math.round(
                    enrollments.stream().mapToInt(Enrollment::getProgress).average().orElse(0)
            );

            List<StudentDto.CourseSummary> courses = enrollments.stream()
                    .map(e -> new StudentDto.CourseSummary(
                            e.getCourse().getId(), e.getCourse().getTitle(), e.getCourse().getAgeRange(),
                            e.getCourse().getCoverImage(), e.getProgress()
                    )).toList();

            return new StudentDto(
                    student.getId(), student.getName(), student.getEmail(), student.getPhone(),
                    student.getStatus(), avgProgress, student.getUpdatedAt(), courses
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public StudentDetailDto getStudent(String id) {
        User student = userRepository.findById(id)
                .filter(u -> u.getRole() == Role.STUDENT)
                .orElseThrow(() -> ApiException.notFound("Alumno no encontrado"));

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(student.getId());
        List<Purchase> purchases = purchaseRepository.findByUserIdOrderByCreatedAtDesc(student.getId());
        List<TaskSubmission> tasks = taskSubmissionRepository.findByUserIdOrderByDeliveredAtDesc(student.getId());

        List<StudentDetailDto.CourseSummary> courseSummaries = enrollments.stream()
                .map(e -> new StudentDetailDto.CourseSummary(
                        e.getCourse().getId(), e.getCourse().getTitle(), e.getCourse().getAgeRange(),
                        e.getCourse().getCoverImage(), e.getProgress(), e.getStatus(), e.getSource()
                )).toList();

        List<StudentDetailDto.PurchaseSummary> purchaseSummaries = purchases.stream()
                .map(p -> new StudentDetailDto.PurchaseSummary(
                        p.getId(), p.getCourse().getTitle(), p.getAmount(), p.getStatus(),
                        p.getMethod(), p.getPaymentId(), p.getCreatedAt()
                )).toList();

        List<StudentDetailDto.TaskSummary> taskSummaries = tasks.stream()
                .map(t -> new StudentDetailDto.TaskSummary(
                        t.getId(), t.getLesson().getTitle(), t.getStatus(), t.getDeliveredAt()
                )).toList();

        return new StudentDetailDto(
                student.getId(), student.getName(), student.getEmail(), student.getPhone(),
                student.getStatus(), student.getCreatedAt(), courseSummaries, purchaseSummaries, taskSummaries
        );
    }

    @Transactional
    public void setStudentStatus(String id, UserStatus status) {
        User student = userRepository.findById(id)
                .filter(u -> u.getRole() == Role.STUDENT)
                .orElseThrow(() -> ApiException.notFound("Alumno no encontrado"));
        student.setStatus(status);
        userRepository.save(student);
    }
}
