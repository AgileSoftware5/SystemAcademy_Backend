package io.github.agilesoftware5.systemacademy.identity.application.internal.queryservices;

import io.github.agilesoftware5.systemacademy.identity.application.queryservices.UserQueryService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.GetCurrentUserQuery;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private static final Logger log = LoggerFactory.getLogger(UserQueryServiceImpl.class);

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetCurrentUserQuery query) {
        var user = userRepository.findById(query.userId());
        if (user.isEmpty()) {
            log.warn("token válido de un usuario inexistente userId={}", query.userId());
            return Optional.empty();
        }
        if (!user.get().isActive()) {
            log.warn("token válido de una cuenta no activa userId={}", query.userId());
            return Optional.empty();
        }
        return user;
    }
}
