package com.alivos.api.dto;

import com.alivos.api.entity.ResourceType;
import lombok.Data;

import java.util.List;

@Data
public class LearningResourceRequest {
    private String title;
    private String description;
    private ResourceType type;
    private String coverImage;
    private String fileUrl;
    private String vimeoUrl;
    private String content;
    private String externalUrl;
    private Boolean visible;
    private Integer order;
    private List<ResourceAttachmentRequest> attachments;
}
