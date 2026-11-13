package io.github.agilesoftware5.systemacademy.identity.application.commandservices;

import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.AuthenticatedSession;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.LoginCommand;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

public interface LoginCommandService {

    Result<AuthenticatedSession> handle(LoginCommand command);
}
