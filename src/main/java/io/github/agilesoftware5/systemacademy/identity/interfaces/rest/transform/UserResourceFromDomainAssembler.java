package io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.UserResource;

public final class UserResourceFromDomainAssembler {

    private UserResourceFromDomainAssembler() {
    }

    public static UserResource toResource(User user) {
        return new UserResource(user.getId(), user.getEmail().value(), user.getFullName(), user.getRole().name(), user.getStatus().name());
    }
}
