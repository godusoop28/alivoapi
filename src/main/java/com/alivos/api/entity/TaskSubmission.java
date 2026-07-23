package com.alivos.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "task_submissions")
@Getter
@Setter
public class TaskSubmission {

    @Id
    @Column(length = 36, updatable = false, nullable = false)
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(length = 4000)
    private String answer;

    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskSubmissionStatus status = TaskSubmissionStatus.PENDING;

    @Column(length = 4000)
    private String adminComment;

    @Column(nullable = false)
    private Instant deliveredAt;

    private Instant reviewedAt;

    @PrePersist
    protected void onCreate() {
        if (deliveredAt == null) {
            deliveredAt = Instant.now();
        }
    }
}
