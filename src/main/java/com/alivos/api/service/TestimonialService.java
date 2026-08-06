package com.alivos.api.service;

import com.alivos.api.dto.TestimonialDto;
import com.alivos.api.dto.TestimonialRequest;
import com.alivos.api.entity.Testimonial;
import com.alivos.api.entity.TestimonialStatus;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.TestimonialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    @Transactional(readOnly = true)
    public List<TestimonialDto> listAll() {
        return testimonialRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public TestimonialDto create(TestimonialRequest input) {
        if (isBlank(input.getAuthorName()) || isBlank(input.getComment())) {
            throw ApiException.badRequest("El nombre y el comentario son obligatorios");
        }
        Testimonial testimonial = new Testimonial();
        applyInput(testimonial, input);
        testimonial = testimonialRepository.save(testimonial);
        return toDto(testimonial);
    }

    @Transactional
    public TestimonialDto update(String id, TestimonialRequest input) {
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Testimonio no encontrado"));
        applyInput(testimonial, input);
        testimonial = testimonialRepository.save(testimonial);
        return toDto(testimonial);
    }

    @Transactional
    public void delete(String id) {
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Testimonio no encontrado"));
        testimonialRepository.delete(testimonial);
    }

    private void applyInput(Testimonial testimonial, TestimonialRequest input) {
        if (input.getAuthorName() != null) testimonial.setAuthorName(input.getAuthorName());
        if (input.getAuthorContext() != null) testimonial.setAuthorContext(input.getAuthorContext());
        if (input.getPhotoUrl() != null) testimonial.setPhotoUrl(input.getPhotoUrl());
        if (input.getRating() != null) testimonial.setRating(input.getRating());
        if (input.getComment() != null) testimonial.setComment(input.getComment());
        if (input.getStatus() != null) testimonial.setStatus(input.getStatus());
        if (input.getDisplayOrder() != null) testimonial.setDisplayOrder(input.getDisplayOrder());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    TestimonialDto toDto(Testimonial t) {
        return new TestimonialDto(
                t.getId(), t.getAuthorName(), t.getAuthorContext(), t.getPhotoUrl(),
                t.getRating(), t.getComment(), t.getStatus(), t.getDisplayOrder(), t.getCreatedAt()
        );
    }

    List<Testimonial> listPublished() {
        return testimonialRepository.findByStatusOrderByDisplayOrderAscCreatedAtDesc(TestimonialStatus.PUBLISHED);
    }
}
