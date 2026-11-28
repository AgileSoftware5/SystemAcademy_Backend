package io.github.agilesoftware5.systemacademy.identity.application.commandservices;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.ChangeUserStatusCommand;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

public interface ChangeUserStatusCommandService {

    Result<User> handle(ChangeUserStatusCommand command);
}
