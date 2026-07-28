package com.alivos.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "learning_resources")
@Getter
@Setter
public class LearningResource extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceType type = ResourceType.PDF;

    private String coverImage;

    /** PDF (or any other file) uploaded to Cloudinary. */
    private String fileUrl;

    private String vimeoUrl;
    private String vimeoEmbedUrl;
    private String vimeoThumbnail;

    /** Free-form reading content for ARTICLE resources. */
    @Column(columnDefinition = "TEXT")
    private String content;

    private String externalUrl;

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;
}
