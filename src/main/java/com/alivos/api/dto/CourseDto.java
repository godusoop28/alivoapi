package com.alivos.api.dto;

import com.alivos.api.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private String id;
    private String title;
    private String slug;
    private String ageRange;
    private String shortDescription;
    private String longDescription;
    private Integer price;
    private String imageUrl;
    private String bannerImage;
    private CourseStatus status;
    private Long studentsCount;
    private Boolean enrolled;
    private Integer progress;
    private List<ModuleDto> modules;
}
