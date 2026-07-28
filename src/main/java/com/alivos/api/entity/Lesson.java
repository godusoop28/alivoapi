package com.alivos.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lessons")
@Getter
@Setter
public class Lesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LessonType type = LessonType.VIDEO;

    @Column(length = 4000)
    private String description;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    private Integer durationMinutes;

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(nullable = false)
    private Boolean hasMaterial = false;

    private String materialUrl;

    @Column(nullable = false)
    private Boolean hasTask = false;

    @Column(length = 4000)
    private String taskDescription;

    private String vimeoId;
    private String vimeoUrl;
    private String vimeoEmbedUrl;
    private String vimeoThumbnail;

    private String imageUrl;
    private String pdfUrl;
    private String assetType;

    @Column(columnDefinition = "TEXT")
    private String formSchema;
}
