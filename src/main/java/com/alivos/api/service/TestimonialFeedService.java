package com.alivos.api.service;

import com.alivos.api.dto.HomeTestimonialDto;
import com.alivos.api.entity.CourseReview;
import com.alivos.api.entity.Testimonial;
import com.alivos.api.repository.CourseReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Combines admin-written Testimonials with the best real CourseReviews into a
 * single feed for the "Lo que dicen las familias" section on the home page.
 */
@Service
@RequiredArgsConstructor
public class TestimonialFeedService {

    private static final int MAX_ITEMS = 12;

    private final TestimonialService testimonialService;
    private final CourseReviewRepository courseReviewRepository;

    @Transactional(readOnly = true)
    public List<HomeTestimonialDto> getHomeFeed() {
        List<HomeTestimonialDto> feed = new ArrayList<>();

        for (Testimonial t : testimonialService.listPublished()) {
            feed.add(new HomeTestimonialDto(t.getAuthorName(), t.getAuthorContext(), t.getPhotoUrl(), t.getRating(), t.getComment(), t.getCreatedAt()));
            if (feed.size() >= MAX_ITEMS) return feed;
        }

        for (CourseReview r : courseReviewRepository.findApprovedWithCommentOrderByRatingDesc()) {
            feed.add(new HomeTestimonialDto(r.getUser().getName(), null, null, r.getRating(), r.getComment(), r.getCreatedAt()));
            if (feed.size() >= MAX_ITEMS) break;
        }

        return feed;
    }
}
