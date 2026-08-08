package com.alivos.api.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    @Column(nullable = false, columnDefinition = "varchar(20)")
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

    /** JSON array of {id, text} — materials/checklist shown to the student. */
    @Column(columnDefinition = "TEXT")
    private String checklistItems;

    @Column(nullable = false)
    private Boolean commentsEnabled = true;

    @Column(nullable = false)
    private Boolean advisoryEnabled = true;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<LessonAttachment> attachments = new ArrayList<>();
}
