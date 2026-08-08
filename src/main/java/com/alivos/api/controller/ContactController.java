package com.alivos.api.controller;

import com.alivos.api.dto.ContactMessageDto;
import com.alivos.api.dto.ContactMessageRequest;
import com.alivos.api.service.ContactMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    public ContactMessageDto submit(@Valid @RequestBody ContactMessageRequest request) {
        return contactMessageService.submit(request);
    }
}
