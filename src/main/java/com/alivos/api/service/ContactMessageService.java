package com.alivos.api.service;

import com.alivos.api.dto.ContactMessageDto;
import com.alivos.api.dto.ContactMessageRequest;
import com.alivos.api.entity.ContactMessage;
import com.alivos.api.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    @Transactional
    public ContactMessageDto submit(ContactMessageRequest input) {
        ContactMessage message = new ContactMessage();
        message.setName(input.getName());
        message.setLastName(input.getLastName());
        message.setEmail(input.getEmail());
        message.setMessage(input.getMessage());
        message.setRead(false);
        message = contactMessageRepository.save(message);
        return toDto(message);
    }

    @Transactional(readOnly = true)
    public List<ContactMessageDto> listAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    private ContactMessageDto toDto(ContactMessage message) {
        return new ContactMessageDto(
                message.getId(),
                message.getName(),
                message.getLastName(),
                message.getEmail(),
                message.getMessage(),
                message.getRead(),
                message.getCreatedAt()
        );
    }
}
