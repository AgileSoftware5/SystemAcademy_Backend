package io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.RegisterUserCommand;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.RegisterUserResource;

public final class RegisterUserCommandFromResourceAssembler {

    private RegisterUserCommandFromResourceAssembler() {
    }

    public static RegisterUserCommand toCommand(RegisterUserResource resource) {
        Role role = Role.valueOf(resource.role().toUpperCase());
        return new RegisterUserCommand(resource.email(), resource.password(), resource.fullName(), role);
    }
}
