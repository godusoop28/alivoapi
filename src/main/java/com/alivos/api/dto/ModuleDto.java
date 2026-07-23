package com.alivos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {
    private String id;
    private String title;
    private String description;
    private Integer order;
    private String coverImage;
    private String bannerImage;
    private List<LessonDto> lessons;
}
