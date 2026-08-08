package com.alivos.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
public class ContactMessage extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(length = 4000, nullable = false)
    private String message;

    @Column(nullable = false)
    private Boolean read = false;
}
