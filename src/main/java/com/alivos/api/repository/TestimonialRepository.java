package com.alivos.api.repository;

import com.alivos.api.entity.Testimonial;
import com.alivos.api.entity.TestimonialStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestimonialRepository extends JpaRepository<Testimonial, String> {
    List<Testimonial> findAllByOrderByCreatedAtDesc();
    List<Testimonial> findByStatusOrderByDisplayOrderAscCreatedAtDesc(TestimonialStatus status);
}
