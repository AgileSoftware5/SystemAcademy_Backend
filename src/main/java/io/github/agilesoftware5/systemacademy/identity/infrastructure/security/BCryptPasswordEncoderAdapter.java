package io.github.agilesoftware5.systemacademy.identity.infrastructure.security;

import io.github.agilesoftware5.systemacademy.identity.domain.services.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {

    private final org.springframework.security.crypto.password.PasswordEncoder delegate;

    public BCryptPasswordEncoderAdapter(org.springframework.security.crypto.password.PasswordEncoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}
