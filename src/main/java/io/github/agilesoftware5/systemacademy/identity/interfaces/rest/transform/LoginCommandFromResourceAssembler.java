package io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.LoginCommand;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.LoginResource;

public final class LoginCommandFromResourceAssembler {

    private LoginCommandFromResourceAssembler() {
    }

    public static LoginCommand toCommand(LoginResource resource) {
        return new LoginCommand(resource.email(), resource.password());
    }
}
