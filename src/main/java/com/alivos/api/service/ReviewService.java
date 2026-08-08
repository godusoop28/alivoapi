package com.alivos.api.service;

import com.alivos.api.dto.AdminCourseReviewDto;
import com.alivos.api.dto.CourseReviewDto;
import com.alivos.api.dto.CourseReviewRequest;
import com.alivos.api.entity.Course;
import com.alivos.api.entity.CourseReview;
import com.alivos.api.entity.CourseReviewStatus;
import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.EnrollmentStatus;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.CourseRepository;
import com.alivos.api.repository.CourseReviewRepository;
import com.alivos.api.repository.EnrollmentRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public CourseReviewDto submitReview(String userId, String slug, CourseReviewRequest input) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> ApiException.notFound("Curso no encontrado"));

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, course.getId())
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .orElseThrow(() -> ApiException.forbidden("Debes estar inscrito en el curso para dejar una reseña"));

        if (enrollment.getProgress() == null || enrollment.getProgress() < 100) {
            throw ApiException.badRequest("Termina el curso para poder dejar una reseña");
        }

        CourseReview review = courseReviewRepository.findByUserIdAndCourseId(userId, course.getId())
                .orElseGet(CourseReview::new);
        if (review.getUser() == null) {
            review.setUser(userRepository.getReferenceById(userId));
            review.setCourse(course);
        }
        review.setRating(input.getRating());
        review.setComment(input.getComment());
        review.setMediaUrl(input.getMediaUrl());
        review.setMediaType(input.getMediaType());
        review = courseReviewRepository.save(review);

        return toDto(review);
    }

    @Transactional(readOnly = true)
    public List<AdminCourseReviewDto> listAllForAdmin() {
        return courseReviewRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(r -> new AdminCourseReviewDto(
                        r.getId(),
                        r.getCourse().getTitle(),
                        r.getUser().getName(),
                        r.getRating(),
                        r.getComment(),
                        r.getMediaUrl(),
                        r.getMediaType(),
                        r.getStatus(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void updateStatus(String id, CourseReviewStatus status) {
        CourseReview review = courseReviewRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Reseña no encontrada"));
        review.setStatus(status);
        courseReviewRepository.save(review);
    }

    public CourseReviewDto toDto(CourseReview review) {
        return new CourseReviewDto(
                review.getId(),
                review.getUser().getName(),
                review.getRating(),
                review.getComment(),
                review.getMediaUrl(),
                review.getMediaType(),
                review.getCreatedAt()
        );
    }
}
