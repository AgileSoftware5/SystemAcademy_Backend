package io.github.agilesoftware5.systemacademy.identity.domain.services;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;

/** Puerto: el dominio pide un token sin conocer JWT ni ninguna librería concreta. */
public interface TokenIssuer {

    String issue(User user);
}
