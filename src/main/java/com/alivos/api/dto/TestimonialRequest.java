package com.alivos.api.dto;

import com.alivos.api.entity.TestimonialStatus;
import lombok.Data;

@Data
public class TestimonialRequest {
    private String authorName;
    private String authorContext;
    private String photoUrl;
    private Integer rating;
    private String comment;
    private TestimonialStatus status;
    private Integer displayOrder;
}
