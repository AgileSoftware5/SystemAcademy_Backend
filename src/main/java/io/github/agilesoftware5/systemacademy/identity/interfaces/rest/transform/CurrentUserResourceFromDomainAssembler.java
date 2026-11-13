package io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.CurrentUserResource;

public final class CurrentUserResourceFromDomainAssembler {

    private CurrentUserResourceFromDomainAssembler() {
    }

    public static CurrentUserResource toResource(User user) {
        return new CurrentUserResource(user.getId(), user.getEmail().value(), user.getFullName(), user.getRole().name());
    }
}
