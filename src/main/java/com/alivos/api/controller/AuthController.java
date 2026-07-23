package com.alivos.api.controller;

import com.alivos.api.dto.AuthUserDto;
import com.alivos.api.dto.LoginRequest;
import com.alivos.api.dto.LoginResponse;
import com.alivos.api.security.SecurityUtils;
import com.alivos.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @GetMapping("/me")
    public java.util.Map<String, AuthUserDto> me(Authentication authentication) {
        String userId = SecurityUtils.requireUserId(authentication);
        return java.util.Map.of("user", authService.getMe(userId));
    }
}
