package com.alivos.api.repository;

import com.alivos.api.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, String> {
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
}
