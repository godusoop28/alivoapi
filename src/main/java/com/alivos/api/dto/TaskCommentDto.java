package com.alivos.api.dto;

import com.alivos.api.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentDto {
    private String id;
    private String authorName;
    private Role authorRole;
    private String text;
    private String fileUrl;
    private Instant createdAt;
}
