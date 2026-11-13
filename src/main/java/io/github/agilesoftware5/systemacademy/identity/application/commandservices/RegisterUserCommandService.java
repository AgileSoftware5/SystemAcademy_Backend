package io.github.agilesoftware5.systemacademy.identity.application.commandservices;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.RegisterUserCommand;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

public interface RegisterUserCommandService {

    Result<User> handle(RegisterUserCommand command);
}
