package io.github.agilesoftware5.systemacademy.identity.application.commandservices;

import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.LogoutCommand;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

public interface LogoutCommandService {

    Result<Void> handle(LogoutCommand command);
}
