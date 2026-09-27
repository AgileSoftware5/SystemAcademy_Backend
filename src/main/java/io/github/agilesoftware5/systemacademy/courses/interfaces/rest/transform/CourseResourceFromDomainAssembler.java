package io.github.agilesoftware5.systemacademy.courses.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources.CourseResource;

public final class CourseResourceFromDomainAssembler {

    private CourseResourceFromDomainAssembler() {
    }

    public static CourseResource toResource(Course course) {
        return new CourseResource(
                course.getId(),
                course.getInstructorId(),
                course.getCode(),
                course.getName(),
                course.getDescription(),
                course.getEnrollmentCode(),
                course.getStatus().name()
        );
    }
}
