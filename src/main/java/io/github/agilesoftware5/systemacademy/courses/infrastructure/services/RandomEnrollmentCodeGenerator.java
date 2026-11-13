package io.github.agilesoftware5.systemacademy.courses.infrastructure.services;

import io.github.agilesoftware5.systemacademy.courses.domain.services.EnrollmentCodeGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Genera códigos de matrícula legibles (sin caracteres ambiguos como 0/O, 1/I). */
@Component
public class RandomEnrollmentCodeGenerator implements EnrollmentCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
