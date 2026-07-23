package com.alivos.api.service;

import com.alivos.api.dto.AuthUserDto;
import com.alivos.api.dto.LoginResponse;
import com.alivos.api.entity.User;
import com.alivos.api.entity.UserStatus;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.UserRepository;
import com.alivos.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> ApiException.unauthorized("Correo o contraseña incorrectos"));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw ApiException.forbidden("Este usuario está bloqueado. Contacta al administrador.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw ApiException.unauthorized("Correo o contraseña incorrectos");
        }

        String token = jwtService.generateToken(user.getId(), user.getRole().name());
        return new LoginResponse(token, toDto(user));
    }

    public AuthUserDto getMe(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
        return toDto(user);
    }

    public static AuthUserDto toDto(User user) {
        return new AuthUserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getStatus()
        );
    }
}
