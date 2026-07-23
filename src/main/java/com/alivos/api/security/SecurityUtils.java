package com.alivos.api.security;

import org.springframework.security.core.Authentication;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String userId && !"anonymousUser".equals(userId)) {
            return userId;
        }
        return null;
    }

    public static String requireUserId(Authentication authentication) {
        String userId = currentUserId(authentication);
        if (userId == null) {
            throw com.alivos.api.exception.ApiException.unauthorized("No autenticado");
        }
        return userId;
    }
}
