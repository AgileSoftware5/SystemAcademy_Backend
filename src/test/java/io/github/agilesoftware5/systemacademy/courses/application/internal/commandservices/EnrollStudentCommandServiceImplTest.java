package io.github.agilesoftware5.systemacademy.courses.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;
import io.github.agilesoftware5.systemacademy.courses.domain.model.commands.EnrollStudentCommand;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.CourseRepository;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Volver a ingresar el código de matrícula es idempotente: devuelve la matrícula activa en vez de un 409. */
class EnrollStudentCommandServiceImplTest {

    CourseRepository courses = mock(CourseRepository.class);
    EnrollmentRepository enrollments = mock(EnrollmentRepository.class);
    EnrollStudentCommandServiceImpl service;

    Course course = Course.create(UUID.randomUUID(), UUID.randomUUID(), "ASI570", "Agile", "desc", "ABC12345");
    UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new EnrollStudentCommandServiceImpl(courses, enrollments);
        when(courses.findByEnrollmentCode("ABC12345")).thenReturn(Optional.of(course));
    }

    @Test
    void enrollsANewStudent() {
        when(enrollments.findByCourseIdAndStudentId(course.getId(), studentId)).thenReturn(Optional.empty());
        when(enrollments.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.handle(new EnrollStudentCommand("ABC12345", studentId));

        assertTrue(result.isSuccess());
        verify(enrollments).save(any());
    }

    @Test
    void repeatingTheCodeReturnsTheExistingActiveEnrollment() {
        var existing = Enrollment.enroll(course.getId(), studentId);
        when(enrollments.findByCourseIdAndStudentId(course.getId(), studentId)).thenReturn(Optional.of(existing));

        var result = service.handle(new EnrollStudentCommand("ABC12345", studentId));

        assertTrue(result.isSuccess());
        assertEquals(existing.getId(), result.getValue().getId());
        verify(enrollments, never()).save(any());
    }

    @Test
    void aDroppedEnrollmentIsNotSilentlyReturned() {
        var dropped = Enrollment.enroll(course.getId(), studentId);
        dropped.drop();
        when(enrollments.findByCourseIdAndStudentId(course.getId(), studentId)).thenReturn(Optional.of(dropped));

        var result = service.handle(new EnrollStudentCommand("ABC12345", studentId));

        assertTrue(result.isFailure());
        verify(enrollments, never()).save(any());
    }
}
