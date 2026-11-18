package io.github.agilesoftware5.systemacademy.shared.interfaces.rest;

import tools.jackson.databind.json.JsonMapper;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.resources.ErrorResource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    public JsonAuthenticationEntryPoint(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResource error = new ErrorResource(
                "UNAUTHORIZED",
                "Se requiere autenticación para acceder a este recurso"
        );

        jsonMapper.writeValue(response.getOutputStream(), error);
    }
}
