package com.alivos.api.dto;

import com.alivos.api.entity.CourseStatus;
import lombok.Data;

@Data
public class CourseRequest {
    private String title;
    private String slug;
    private String ageRange;
    private String shortDescription;
    private String longDescription;
    private Integer price;
    private String coverImage;
    private String bannerImage;
    private String previewVimeoUrl;
    private CourseStatus status;
}
