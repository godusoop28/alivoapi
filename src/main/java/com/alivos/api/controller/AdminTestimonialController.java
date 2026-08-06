package com.alivos.api.controller;

import com.alivos.api.dto.TestimonialDto;
import com.alivos.api.dto.TestimonialRequest;
import com.alivos.api.service.TestimonialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/testimonials")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTestimonialController {

    private final TestimonialService testimonialService;

    @GetMapping
    public Map<String, List<TestimonialDto>> listTestimonials() {
        return Map.of("testimonials", testimonialService.listAll());
    }

    @PostMapping
    public Map<String, TestimonialDto> createTestimonial(@RequestBody TestimonialRequest request) {
        return Map.of("testimonial", testimonialService.create(request));
    }

    @PatchMapping("/{id}")
    public Map<String, TestimonialDto> updateTestimonial(@PathVariable String id, @RequestBody TestimonialRequest request) {
        return Map.of("testimonial", testimonialService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> deleteTestimonial(@PathVariable String id) {
        testimonialService.delete(id);
        return Map.of("ok", true);
    }
}
