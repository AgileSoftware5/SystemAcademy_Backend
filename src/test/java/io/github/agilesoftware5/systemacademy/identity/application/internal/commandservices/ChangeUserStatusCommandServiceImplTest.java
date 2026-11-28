package io.github.agilesoftware5.systemacademy.identity.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.ChangeUserStatusCommand;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.UserStatus;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.UserRepository;
import io.github.agilesoftware5.systemacademy.shared.application.audit.AuditEntry;
import io.github.agilesoftware5.systemacademy.shared.application.audit.AuditLog;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChangeUserStatusCommandServiceImplTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final List<AuditEntry> audit = new ArrayList<>();
    private final AuditLog auditLog = audit::add;
    private final ChangeUserStatusCommandServiceImpl service = new ChangeUserStatusCommandServiceImpl(repository, auditLog);
    private final UUID admin = UUID.randomUUID();
    private User student;

    @BeforeEach
    void setUp() {
        student = User.register(new Email("ana@academia.edu"), "hash", "Ana", Role.STUDENT);
        when(repository.findById(student.getId())).thenReturn(Optional.of(student));
        when(repository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void suspendsAnActiveUserAndAuditsIt() {
        var result = service.handle(new ChangeUserStatusCommand(admin, student.getId(), UserStatus.SUSPENDED));

        assertTrue(result.isSuccess());
        assertEquals(UserStatus.SUSPENDED, result.getValue().getStatus());
        assertEquals(1, audit.size());
        assertEquals("USER_SUSPENDED", audit.get(0).action());
        assertEquals(admin, audit.get(0).actorId());
    }

    @Test
    void reactivatesASuspendedUser() {
        student.suspend();

        var result = service.handle(new ChangeUserStatusCommand(admin, student.getId(), UserStatus.ACTIVE));

        assertEquals(UserStatus.ACTIVE, result.getValue().getStatus());
        assertEquals("USER_ACTIVATED", audit.get(0).action());
    }

    @Test
    void doesNothingWhenTheStatusIsAlreadyTheRequestedOne() {
        var result = service.handle(new ChangeUserStatusCommand(admin, student.getId(), UserStatus.ACTIVE));

        assertTrue(result.isSuccess());
        verify(repository, never()).save(any());
        assertTrue(audit.isEmpty());
    }

    @Test
    void anAdminCannotChangeTheirOwnStatus() {
        var result = service.handle(new ChangeUserStatusCommand(admin, admin, UserStatus.SUSPENDED));

        assertEquals(ApplicationErrorCode.BUSINESS_RULE_VIOLATION, result.getError().code());
    }

    @Test
    void anotherAdminCannotBeSuspended() {
        var other = User.register(new Email("root@academia.edu"), "hash", "Root", Role.ADMIN);
        when(repository.findById(other.getId())).thenReturn(Optional.of(other));

        var result = service.handle(new ChangeUserStatusCommand(admin, other.getId(), UserStatus.SUSPENDED));

        assertEquals(ApplicationErrorCode.BUSINESS_RULE_VIOLATION, result.getError().code());
        verify(repository, never()).save(any());
    }

    @Test
    void unknownUserIsNotFound() {
        var result = service.handle(new ChangeUserStatusCommand(admin, UUID.randomUUID(), UserStatus.SUSPENDED));

        assertEquals(ApplicationErrorCode.NOT_FOUND, result.getError().code());
    }

    @Test
    void inactiveIsNotAnAllowedTarget() {
        var result = service.handle(new ChangeUserStatusCommand(admin, student.getId(), UserStatus.INACTIVE));

        assertEquals(ApplicationErrorCode.VALIDATION_ERROR, result.getError().code());
    }
}
