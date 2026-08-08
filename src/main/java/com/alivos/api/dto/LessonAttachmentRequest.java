package com.alivos.api.dto;

import lombok.Data;

@Data
public class LessonAttachmentRequest {
    private String id;
    private String type;
    private String title;
    private String description;
    private String fileUrl;
    private String externalUrl;
    private String formSchema;
    private Integer order;
}
