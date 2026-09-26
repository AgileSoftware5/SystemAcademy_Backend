package io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.AuthenticatedSession;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.AuthenticatedSessionResource;

public final class AuthenticatedSessionResourceFromDomainAssembler {

    private AuthenticatedSessionResourceFromDomainAssembler() {
    }

    public static AuthenticatedSessionResource toResource(AuthenticatedSession session) {
        return new AuthenticatedSessionResource(session.token(), session.userId(), session.role().name());
    }
}
