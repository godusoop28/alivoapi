package com.alivos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAttachmentDto {
    private String id;
    private String title;
    private String description;
    private String fileUrl;
    private String externalUrl;
    private Integer order;
}
