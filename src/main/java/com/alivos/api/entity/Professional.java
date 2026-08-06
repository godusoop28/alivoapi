package com.alivos.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "professionals")
@Getter
@Setter
public class Professional extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String bio;

    private String photoUrl;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Integer slotMinutes = 30;

    /** Comma-separated ISO day-of-week numbers, 1 (Monday) to 7 (Sunday). */
    @Column(nullable = false)
    private String workDays = "1,2,3,4,5";

    @Column(nullable = false)
    private String startTime = "09:00";

    @Column(nullable = false)
    private String endTime = "18:00";
}
