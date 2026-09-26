package io.github.agilesoftware5.systemacademy.identity.infrastructure.security;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.services.TokenIssuer;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    private final JwtService jwtService;

    public JwtTokenIssuer(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String issue(User user) {
        return jwtService.generate(user.getId().toString(), user.getRole().name());
    }
}
