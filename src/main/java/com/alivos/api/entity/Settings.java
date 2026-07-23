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
}
