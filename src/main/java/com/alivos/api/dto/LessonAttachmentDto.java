package com.alivos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonAttachmentDto {
    private String id;
    private String type;
    private String title;
    private String description;
    private String fileUrl;
    private String externalUrl;
    private String formSchema;
    private Integer order;
}
