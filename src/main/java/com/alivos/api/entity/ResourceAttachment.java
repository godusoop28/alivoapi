package com.alivos.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A single file or link attached to a LearningResource ("Aprende Más" card). */
@Entity
@Table(name = "resource_attachments")
@Getter
@Setter
public class ResourceAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private LearningResource resource;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    /** File uploaded to Cloudinary. */
    private String fileUrl;

    /** External link (Google Drive or any other URL). */
    private String externalUrl;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;
}
