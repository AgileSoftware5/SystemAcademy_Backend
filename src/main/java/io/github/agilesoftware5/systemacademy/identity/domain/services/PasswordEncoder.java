package io.github.agilesoftware5.systemacademy.identity.domain.services;

/** Puerto: el dominio no sabe (ni le importa) qué algoritmo de hash se usa. */
public interface PasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
