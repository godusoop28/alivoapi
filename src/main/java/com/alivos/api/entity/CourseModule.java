package com.alivos.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "course_modules")
@Getter
@Setter
public class CourseModule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Column(nullable = false)
    private Boolean visible = true;

    private String coverImage;

    private String bannerImage;
}
