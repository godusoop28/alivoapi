package com.alivos.api.dto;

import com.alivos.api.entity.TestimonialStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialDto {
    private String id;
    private String authorName;
    private String authorContext;
    private String photoUrl;
    private Integer rating;
    private String comment;
    private TestimonialStatus status;
    private Integer displayOrder;
    private Instant createdAt;
}
