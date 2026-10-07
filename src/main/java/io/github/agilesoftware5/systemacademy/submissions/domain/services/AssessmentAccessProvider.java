package io.github.agilesoftware5.systemacademy.submissions.domain.services;

import java.time.Instant;
import java.util.UUID;

/** Lo que submissions necesita saber de una evaluación, sin depender del modelo de assessments. */
public interface AssessmentAccessProvider {

    boolean isTaughtBy(UUID assessmentId, UUID teacherId);

    boolean areResultsReleased(UUID assessmentId);

    boolean isOpenForSubmissions(UUID assessmentId, Instant now);

    boolean isStudentEnrolled(UUID assessmentId, UUID studentId);

    boolean hasQuestion(UUID assessmentId, UUID questionId);
}
