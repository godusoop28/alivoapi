package com.alivos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VimeoResolvedDto {
    private String vimeoId;
    private String title;
    private String thumbnailUrl;
    private Integer duration;
    private String embedUrl;
}
