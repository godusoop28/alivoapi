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

/**
 * A lesson can have any number of attachments: downloadable/linked PDFs,
 * general links (e.g. Google Drive folders) or surveys (SURVEY carries its
 * own formSchema, independent from the legacy Lesson.formSchema used by
 * FORM-type lessons).
 */
@Entity
@Table(name = "lesson_attachments")
@Getter
@Setter
public class LessonAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private LessonAttachmentType type = LessonAttachmentType.PDF;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    /** File uploaded to Cloudinary. */
    private String fileUrl;

    /** External link (Google Drive or any other URL). */
    private String externalUrl;

    /** JSON array of FormField, only set when type = SURVEY. */
    @Column(columnDefinition = "TEXT")
    private String formSchema;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;
}
