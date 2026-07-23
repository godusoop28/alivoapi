package com.alivos.api.controller;

import com.alivos.api.dto.VimeoResolveRequest;
import com.alivos.api.dto.VimeoResolvedDto;
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
@PreAuthorize("hasRole('ADMIN')")
public class VimeoController {

    private final VimeoService vimeoService;

    @PostMapping("/resolve")
    public VimeoResolvedDto resolve(@Valid @RequestBody VimeoResolveRequest request) {
        return vimeoService.resolve(request.getUrl());
    }
}
