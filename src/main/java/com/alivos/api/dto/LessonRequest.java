package com.alivos.api.dto;

import com.alivos.api.entity.LessonType;
import lombok.Data;

@Data
public class LessonRequest {
    private String title;
    private LessonType type;
    private String description;
    private Integer order;
    private Integer durationMinutes;
    private Boolean visible;
    private Boolean hasMaterial;
    private String materialUrl;
    private Boolean hasTask;
    private String taskDescription;
    private String vimeoUrl;
    private String imageUrl;
    private String pdfUrl;
    private String assetType;
    private String formSchema;
}
