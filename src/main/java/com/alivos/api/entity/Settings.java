package com.alivos.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "settings")
@Getter
@Setter
public class Settings extends BaseEntity {

    public static final String SINGLETON_ID = "singleton";

    private String whatsapp;
    private String email;
    private String appointmentUrl;
    private String instagram;
    private String facebook;
    private String website;
    private String brandName;

    private Boolean advisoryEnabled = true;
    private Integer advisoryPrice = 0;
    private Integer advisorySlotMinutes = 30;
    /** Comma-separated ISO day-of-week numbers, 1 (Monday) to 7 (Sunday). */
    private String advisoryDays = "1,2,3,4,5";
    private String advisoryStartTime = "09:00";
    private String advisoryEndTime = "18:00";
}
