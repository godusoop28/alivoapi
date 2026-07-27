package com.alivos.api.controller;

import com.alivos.api.dto.VimeoResolveRequest;
import com.alivos.api.dto.VimeoResolvedDto;
import com.alivos.api.dto.VimeoUploadTicketDto;
import com.alivos.api.dto.VimeoUploadTicketRequest;
import com.alivos.api.service.VimeoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vimeo")
@RequiredArgsConstructor
public class VimeoController {

    private final VimeoService vimeoService;

    @PostMapping("/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public VimeoResolvedDto resolve(@Valid @RequestBody VimeoResolveRequest request) {
        return vimeoService.resolve(request.getUrl());
    }

    // Any authenticated user (not just admins) can request a ticket: students
    // use this to attach a video directly to a task submission.
    @PostMapping("/upload-ticket")
    public VimeoUploadTicketDto uploadTicket(@Valid @RequestBody VimeoUploadTicketRequest request) {
        return vimeoService.createUploadTicket(request.getFileName(), request.getFileSizeBytes());
    }
}
