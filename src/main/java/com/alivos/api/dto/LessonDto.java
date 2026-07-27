package com.alivos.api.dto;

import com.alivos.api.entity.LessonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
    private String id;
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
    private String vimeoId;
    private String vimeoUrl;
    private String vimeoEmbedUrl;
    private String vimeoThumbnail;
    private String imageUrl;
    private String pdfUrl;
    private String assetType;
    private Boolean completed;
    private Boolean locked;
}
