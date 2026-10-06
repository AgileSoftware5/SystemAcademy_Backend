package io.github.agilesoftware5.systemacademy.submissions.support;

import io.github.agilesoftware5.systemacademy.submissions.domain.services.AssessmentAccessProvider;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FakeAssessmentAccess implements AssessmentAccessProvider {

    private final UUID teacherId;
    public boolean resultsReleased;
    public boolean open = true;
    public boolean enrolled = true;
    public final Set<UUID> questionIds = new HashSet<>();

    public FakeAssessmentAccess(UUID teacherId) {
        this.teacherId = teacherId;
    }

    @Override
    public boolean isTaughtBy(UUID assessmentId, UUID teacherId) {
        return this.teacherId.equals(teacherId);
    }

    @Override
    public boolean areResultsReleased(UUID assessmentId) {
        return resultsReleased;
    }

    @Override
    public boolean isOpenForSubmissions(UUID assessmentId, Instant now) {
        return open;
    }

    @Override
    public boolean isStudentEnrolled(UUID assessmentId, UUID studentId) {
        return enrolled;
    }

    @Override
    public boolean hasQuestion(UUID assessmentId, UUID questionId) {
        return questionIds.contains(questionId);
    }
}
