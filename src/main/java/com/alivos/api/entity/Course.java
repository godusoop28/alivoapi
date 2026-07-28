package com.alivos.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class Course extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String ageRange;

    @Column(nullable = false, length = 1000)
    private String shortDescription;

    @Column(nullable = false, length = 8000)
    private String longDescription;

    @Column(nullable = false)
    private Integer price;

    private String coverImage;

    private String bannerImage;

    private String previewVimeoUrl;
    private String previewVimeoEmbedUrl;
    private String previewVimeoThumbnail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status = CourseStatus.DRAFT;
}
