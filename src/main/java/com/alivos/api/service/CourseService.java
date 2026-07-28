package com.alivos.api.service;

import com.alivos.api.dto.CourseDto;
import com.alivos.api.dto.CourseRequest;
import com.alivos.api.dto.CourseReviewDto;
import com.alivos.api.dto.LessonDto;
import com.alivos.api.dto.ModuleDto;
import com.alivos.api.entity.Course;
import com.alivos.api.entity.CourseModule;
import com.alivos.api.entity.CourseReview;
import com.alivos.api.entity.CourseStatus;
import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.EnrollmentStatus;
import com.alivos.api.entity.Lesson;
import com.alivos.api.entity.LessonProgress;
import com.alivos.api.dto.VimeoResolvedDto;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.CourseModuleRepository;
import com.alivos.api.repository.CourseRepository;
import com.alivos.api.repository.CourseReviewRepository;
import com.alivos.api.repository.EnrollmentRepository;
import com.alivos.api.repository.LessonProgressRepository;
import com.alivos.api.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private static final int MAX_REVIEWS_RETURNED = 20;

    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final VimeoService vimeoService;

    @Transactional(readOnly = true)
    public List<CourseDto> listPublicCourses(String userId) {
        List<Course> courses = courseRepository.findByStatusOrderByCreatedAtAsc(CourseStatus.PUBLISHED);
        Map<String, Enrollment> enrollmentByCourseId = activeEnrollmentsByCourseId(userId);

        return courses.stream()
                .map(course -> toDto(course, userId, enrollmentByCourseId.get(course.getId()), false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseBySlug(String slug, String userId) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> ApiException.notFound("Curso no encontrado"));
        if (course.getStatus() == CourseStatus.HIDDEN) {
            throw ApiException.notFound("Curso no encontrado");
        }

        Enrollment enrollment = null;
        if (userId != null) {
            Optional<Enrollment> found = enrollmentRepository.findByUserIdAndCourseId(userId, course.getId());
            if (found.isPresent() && found.get().getStatus() == EnrollmentStatus.ACTIVE) {
                enrollment = found.get();
            }
        }

        return toDto(course, userId, enrollment, false);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> listMyCourses(String userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(userId, EnrollmentStatus.ACTIVE);
        return enrollments.stream()
                .map(e -> toDto(e.getCourse(), userId, e, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDto> listAdminCourses() {
        return courseRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(course -> toDto(course, null, null, true))
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseDto createCourse(CourseRequest input) {
        if (isBlank(input.getTitle()) || isBlank(input.getAgeRange())
                || isBlank(input.getShortDescription()) || input.getPrice() == null) {
            throw ApiException.badRequest("title, ageRange, shortDescription y price son obligatorios");
        }

        String slug = !isBlank(input.getSlug()) ? input.getSlug().trim() : slugify(input.getTitle());
        if (courseRepository.existsBySlug(slug)) {
            throw ApiException.conflict("Ya existe un curso con ese slug");
        }

        Course course = new Course();
        course.setTitle(input.getTitle());
        course.setSlug(slug);
        course.setAgeRange(input.getAgeRange());
        course.setShortDescription(input.getShortDescription());
        course.setLongDescription(!isBlank(input.getLongDescription()) ? input.getLongDescription() : input.getShortDescription());
        course.setPrice(input.getPrice());
        course.setCoverImage(input.getCoverImage());
        course.setBannerImage(input.getBannerImage());
        course.setStatus(input.getStatus() != null ? input.getStatus() : CourseStatus.DRAFT);
        applyPreviewVideo(course, input.getPreviewVimeoUrl());

        course = courseRepository.save(course);
        return toDto(course, null, null, true);
    }

    @Transactional
    public CourseDto updateCourse(String id, CourseRequest input) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Curso no encontrado"));

        if (input.getTitle() != null) course.setTitle(input.getTitle());
        if (input.getSlug() != null) course.setSlug(input.getSlug());
        if (input.getAgeRange() != null) course.setAgeRange(input.getAgeRange());
        if (input.getShortDescription() != null) course.setShortDescription(input.getShortDescription());
        if (input.getLongDescription() != null) course.setLongDescription(input.getLongDescription());
        if (input.getPrice() != null) course.setPrice(input.getPrice());
        if (input.getCoverImage() != null) course.setCoverImage(input.getCoverImage());
        if (input.getBannerImage() != null) course.setBannerImage(input.getBannerImage());
        if (input.getStatus() != null) course.setStatus(input.getStatus());
        if (input.getPreviewVimeoUrl() != null) applyPreviewVideo(course, input.getPreviewVimeoUrl());

        course = courseRepository.save(course);
        return toDto(course, null, null, true);
    }

    private void applyPreviewVideo(Course course, String previewVimeoUrl) {
        if (previewVimeoUrl == null || previewVimeoUrl.isBlank()) {
            course.setPreviewVimeoUrl(null);
            course.setPreviewVimeoEmbedUrl(null);
            course.setPreviewVimeoThumbnail(null);
            return;
        }
        VimeoResolvedDto resolved = vimeoService.resolve(previewVimeoUrl);
        course.setPreviewVimeoUrl(previewVimeoUrl);
        course.setPreviewVimeoEmbedUrl(resolved.getEmbedUrl());
        course.setPreviewVimeoThumbnail(resolved.getThumbnailUrl());
    }

    @Transactional
    public void hideCourse(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Curso no encontrado"));
        course.setStatus(CourseStatus.HIDDEN);
        courseRepository.save(course);
    }

    // ----- DTO mapping -----

    private Map<String, Enrollment> activeEnrollmentsByCourseId(String userId) {
        if (userId == null) return Map.of();
        return enrollmentRepository.findByUserIdAndStatus(userId, EnrollmentStatus.ACTIVE).stream()
                .collect(Collectors.toMap(e -> e.getCourse().getId(), e -> e));
    }

    private CourseDto toDto(Course course, String userId, Enrollment enrollment, boolean forceAccess) {
        long studentsCount = enrollmentRepository.countByCourseIdAndStatus(course.getId(), EnrollmentStatus.ACTIVE);
        boolean hasAccess = forceAccess || enrollment != null;

        Map<String, Boolean> progressByLessonId = new HashMap<>();
        if (userId != null) {
            List<LessonProgress> progress = lessonProgressRepository.findByUserIdAndCourseId(userId, course.getId());
            for (LessonProgress p : progress) {
                progressByLessonId.put(p.getLesson().getId(), p.getCompleted());
            }
        }

        List<ModuleDto> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(course.getId()).stream()
                .filter(CourseModule::getVisible)
                .map(module -> toModuleDto(module, progressByLessonId, hasAccess))
                .collect(Collectors.toList());

        List<CourseReview> reviews = courseReviewRepository.findByCourseIdOrderByCreatedAtDesc(course.getId());
        Double averageRating = courseReviewRepository.averageRatingForCourse(course.getId());
        CourseReviewDto myReview = userId == null ? null : reviews.stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .findFirst()
                .map(this::toReviewDto)
                .orElse(null);
        boolean canReview = enrollment != null && enrollment.getProgress() != null && enrollment.getProgress() >= 100;

        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setSlug(course.getSlug());
        dto.setAgeRange(course.getAgeRange());
        dto.setShortDescription(course.getShortDescription());
        dto.setLongDescription(course.getLongDescription());
        dto.setPrice(course.getPrice());
        dto.setImageUrl(course.getCoverImage());
        dto.setBannerImage(course.getBannerImage());
        dto.setPreviewVimeoUrl(course.getPreviewVimeoUrl());
        dto.setPreviewVimeoEmbedUrl(course.getPreviewVimeoEmbedUrl());
        dto.setPreviewVimeoThumbnail(course.getPreviewVimeoThumbnail());
        dto.setStatus(course.getStatus());
        dto.setStudentsCount(studentsCount);
        dto.setEnrolled(enrollment != null);
        dto.setProgress(enrollment != null ? enrollment.getProgress() : 0);
        dto.setHasAccess(hasAccess);
        dto.setAverageRating(averageRating);
        dto.setReviewsCount(reviews.size());
        dto.setReviews(reviews.stream().limit(MAX_REVIEWS_RETURNED).map(this::toReviewDto).toList());
        dto.setMyReview(myReview);
        dto.setCanReview(canReview);
        dto.setModules(modules);
        return dto;
    }

    private CourseReviewDto toReviewDto(CourseReview review) {
        return new CourseReviewDto(
                review.getId(),
                review.getUser().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private ModuleDto toModuleDto(CourseModule module, Map<String, Boolean> progressByLessonId, boolean hasAccess) {
        List<LessonDto> lessons = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId()).stream()
                .filter(Lesson::getVisible)
                .map(lesson -> CourseMapper.toLessonDto(lesson, progressByLessonId, hasAccess))
                .collect(Collectors.toList());

        return CourseMapper.toModuleDto(module, lessons);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-|-$)");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static String slugify(String title) {
        String normalized = Normalizer.normalize(title.toLowerCase(), Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS.matcher(normalized).replaceAll("");
        String slug = NON_ALPHANUMERIC.matcher(withoutDiacritics).replaceAll("-");
        return EDGE_DASHES.matcher(slug).replaceAll("");
    }
}
