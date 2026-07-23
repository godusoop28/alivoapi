package com.alivos.api.dto;

import com.alivos.api.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private UserStatus status;
    private long avgProgress;
    private Instant lastAccess;
    private List<CourseSummary> courses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseSummary {
        private String id;
        private String title;
        private String ageRange;
        private String imageUrl;
        private Integer progress;
    }
}
