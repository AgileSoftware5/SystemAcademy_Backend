package io.github.agilesoftware5.systemacademy.courses.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources.EnrollmentResource;

public final class EnrollmentResourceFromDomainAssembler {

    private EnrollmentResourceFromDomainAssembler() {
    }

    public static EnrollmentResource toResource(Enrollment enrollment) {
        return new EnrollmentResource(enrollment.getId(), enrollment.getCourseId(), enrollment.getStudentId(), enrollment.getStatus().name());
    }
}
