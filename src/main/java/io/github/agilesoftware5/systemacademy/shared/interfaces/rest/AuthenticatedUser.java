package io.github.agilesoftware5.systemacademy.shared.interfaces.rest;

import org.springframework.security.core.Authentication;

import java.util.UUID;

/** El JwtAuthenticationFilter pone el userId como principal (name) del Authentication. */
public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    public static UUID id(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    public static boolean hasRole(Authentication authentication, String role) {
        String authority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals(authority));
    }
}
