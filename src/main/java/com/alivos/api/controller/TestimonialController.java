package com.alivos.api.controller;

import com.alivos.api.dto.HomeTestimonialDto;
import com.alivos.api.service.TestimonialFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private final TestimonialFeedService testimonialFeedService;

    @GetMapping("/home")
    public Map<String, List<HomeTestimonialDto>> homeFeed() {
        return Map.of("testimonials", testimonialFeedService.getHomeFeed());
    }
}
