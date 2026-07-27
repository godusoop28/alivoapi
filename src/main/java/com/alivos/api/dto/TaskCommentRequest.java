package com.alivos.api.dto;

import lombok.Data;

@Data
public class TaskCommentRequest {
    private String text;
    private String fileUrl;
}
