package com.alivos.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "testimonials")
@Getter
@Setter
public class Testimonial extends BaseEntity {

    @Column(nullable = false)
    private String authorName;

    private String authorContext;

    private String photoUrl;

    private Integer rating;

    @Column(nullable = false, length = 2000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TestimonialStatus status = TestimonialStatus.PUBLISHED;

    private Integer displayOrder;
}
