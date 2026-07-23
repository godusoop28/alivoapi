package com.alivos.api.dto;

import lombok.Data;

@Data
public class ModuleRequest {
    private String title;
    private String description;
    private Integer order;
    private Boolean visible;
    private String coverImage;
    private String bannerImage;
}
