package com.alivos.api.controller;

import com.alivos.api.dto.UploadResultDto;
import com.alivos.api.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UploadController {

    private final UploadService uploadService;

    @PostMapping
    public UploadResultDto upload(@RequestParam("file") MultipartFile file) {
        return new UploadResultDto(uploadService.upload(file));
    }
}
