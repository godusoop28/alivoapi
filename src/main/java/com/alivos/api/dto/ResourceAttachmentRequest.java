package com.alivos.api.dto;

import lombok.Data;

@Data
public class ResourceAttachmentRequest {
    private String id;
    private String title;
    private String description;
    private String fileUrl;
    private String externalUrl;
    private Integer order;
}
