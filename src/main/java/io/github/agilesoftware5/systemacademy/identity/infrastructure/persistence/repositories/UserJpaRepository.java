package io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.repositories;

import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.UserStatus;
import io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    boolean existsByIdAndStatus(UUID id, UserStatus status);

    @Query("""
            select u from UserEntity u
            where (:role is null or u.role = :role)
              and (:status is null or u.status = :status)
              and (lower(u.fullName) like :text escape '!' or lower(u.email) like :text escape '!')
            """)
    Page<UserEntity> search(@Param("role") Role role, @Param("status") UserStatus status, @Param("text") String text, Pageable pageable);

    List<UserEntity> findByIdInAndRoleAndStatus(
            List<UUID> ids,
            Role role,
            UserStatus status
    );
}
