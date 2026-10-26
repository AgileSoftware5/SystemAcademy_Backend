package io.github.agilesoftware5.systemacademy.identity.application.internal.queryservices;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.GetCurrentUserQuery;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserQueryServiceImplTest {

    private User user = User.register(new Email("ana@academia.edu"), "hash", "Ana Pérez", Role.TEACHER);

    private final UserRepository repository = new UserRepository() {
        @Override
        public User save(User saved) {
            user = saved;
            return saved;
        }

        @Override
        public Optional<User> findById(UUID id) {
            return user != null && user.getId().equals(id) ? Optional.of(user) : Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(Email email) {
            return Optional.empty();
        }

        @Override
        public boolean existsByEmail(Email email) {
            return false;
        }
    };

    private final UserQueryServiceImpl service = new UserQueryServiceImpl(repository);

    @Test
    void activeUserIsReturned() {
        var result = service.handle(new GetCurrentUserQuery(user.getId()));

        assertTrue(result.isPresent());
        assertEquals("ana@academia.edu", result.get().getEmail().value());
    }

    @Test
    void unknownUserIsEmpty() {
        assertTrue(service.handle(new GetCurrentUserQuery(UUID.randomUUID())).isEmpty());
    }

    @Test
    void suspendedUserIsEmptyEvenWithAValidToken() {
        user.suspend();

        assertTrue(service.handle(new GetCurrentUserQuery(user.getId())).isEmpty());
    }
}
