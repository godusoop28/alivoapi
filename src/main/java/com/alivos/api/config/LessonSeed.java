package com.alivos.api.config;

import com.alivos.api.entity.LessonType;

record LessonSeed(String title, LessonType type, Integer durationMinutes, String description,
                   boolean hasMaterial, String materialUrl, boolean hasTask, String taskDescription,
                   boolean withVimeo, String imageUrl, String pdfUrl, String assetType) {

    static LessonSeed of(String title, LessonType type, int duration, String description, String imageUrl, String assetType) {
        return new LessonSeed(title, type, duration, description, false, null, false, null, false, imageUrl, null, assetType);
    }
}
