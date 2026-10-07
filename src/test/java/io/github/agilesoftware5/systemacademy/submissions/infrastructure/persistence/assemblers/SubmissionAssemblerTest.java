package io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.assemblers;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.CriterionResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;
import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.entities.CriterionResultEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionAssemblerTest {

    @Test
    void persistsCriterionResultsWithTheirRubricOrder() {
        Submission submission = Submission.start(UUID.randomUUID(), UUID.randomUUID());
        Answer answer = Answer.create(submission.getId(), UUID.randomUUID(), "texto");
        submission.addAnswer(answer);
        submission.submit();
        answer.startGrading();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        answer.addCriterionResult(CriterionResult.create(answer.getId(), first, CriterionVerdict.MEETS, "e", "f"));
        answer.addCriterionResult(CriterionResult.create(answer.getId(), second, CriterionVerdict.MEETS, "e", "f"));

        List<CriterionResultEntity> entities =
                SubmissionAssembler.toEntity(submission).getAnswers().get(0).getCriterionResults();

        assertEquals(List.of(first, second), entities.stream().map(CriterionResultEntity::getRubricCriterionId).toList());
        assertEquals(List.of(0, 1), entities.stream().map(CriterionResultEntity::getPosition).toList());
    }
}
