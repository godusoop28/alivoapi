package com.alivos.api.controller;

import com.alivos.api.dto.ContactMessageDto;
import com.alivos.api.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/contact")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminContactController {

    private final ContactMessageService contactMessageService;

    @GetMapping
    public Map<String, List<ContactMessageDto>> list() {
        return Map.of("messages", contactMessageService.listAll());
    }
}
