package com.alivos.api.dto;

import com.alivos.api.entity.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningResourceDto {
    private String id;
    private String title;
    private String description;
    private ResourceType type;
    private String coverImage;
    private String fileUrl;
    private String vimeoUrl;
    private String vimeoEmbedUrl;
    private String vimeoThumbnail;
    private String content;
    private String externalUrl;
    private Boolean visible;
    private Instant createdAt;
    private List<ResourceAttachmentDto> attachments;
}
