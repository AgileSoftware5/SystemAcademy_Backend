package io.github.agilesoftware5.systemacademy.courses.infrastructure.acl.adapters;

import io.github.agilesoftware5.systemacademy.courses.application.dto.StudentProfile;
import io.github.agilesoftware5.systemacademy.courses.application.ports.outbound.StudentIdentityQuery;
import io.github.agilesoftware5.systemacademy.identity.application.queryservices.UserQueryService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.GetStudentProfilesByIdsQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class StudentIdentityQueryAdapter implements StudentIdentityQuery {

    private final UserQueryService userQueryService;

    public StudentIdentityQueryAdapter(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @Override
    public List<StudentProfile> findStudentProfilesByIds(List<UUID> studentIds) {

        if (studentIds == null || studentIds.isEmpty()) {
            return List.of();
        }

        var profiles = userQueryService.handle(
                new GetStudentProfilesByIdsQuery(studentIds)
        );

        return profiles.stream()
                .map(profile -> new StudentProfile(
                        profile.studentId(),
                        profile.fullName(),
                        profile.email()
                ))
                .toList();
    }
}
