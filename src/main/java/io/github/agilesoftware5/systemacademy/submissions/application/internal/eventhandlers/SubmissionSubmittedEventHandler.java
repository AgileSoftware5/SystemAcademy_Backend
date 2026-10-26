package io.github.agilesoftware5.systemacademy.submissions.application.internal.eventhandlers;

import io.github.agilesoftware5.systemacademy.submissions.application.internal.grading.AnswerGrader;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.events.SubmissionSubmittedEvent;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Califica automáticamente cada respuesta al entregar, fuera del request del alumno.
 * Si una respuesta falla queda FAILED y el docente la reintenta a mano; las demás siguen.
 */
@Component
@ConditionalOnProperty(name = "grading.auto-on-submit", havingValue = "true", matchIfMissing = true)
public class SubmissionSubmittedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SubmissionSubmittedEventHandler.class);

    private final SubmissionRepository submissionRepository;
    private final AnswerGrader answerGrader;

    public SubmissionSubmittedEventHandler(SubmissionRepository submissionRepository, AnswerGrader answerGrader) {
        this.submissionRepository = submissionRepository;
        this.answerGrader = answerGrader;
    }

    @Async("gradingExecutor")
    @EventListener
    public void on(SubmissionSubmittedEvent event) {
        var submission = submissionRepository.findById(event.submissionId()).orElse(null);
        if (submission == null) {
            log.warn("calificación automática omitida: entrega no encontrada submissionId={}", event.submissionId());
            return;
        }

        var pending = submission.getAnswers().stream()
                .filter(answer -> answer.getGradingStatus().canStartGrading())
                .map(Answer::getId)
                .toList();

        log.info("calificación automática iniciada submissionId={} respuestas={}", submission.getId(), pending.size());

        long failed = 0;
        for (UUID answerId : pending) {
            // Se recarga por respuesta: tras un fallo al guardar, la copia en memoria queda obsoleta.
            var current = submissionRepository.findById(submission.getId()).orElse(null);
            if (current == null || answerGrader.grade(current, answerId).isFailure()) {
                failed++;
            }
        }

        log.info("calificación automática terminada submissionId={} ok={} fallidas={}",
                submission.getId(), pending.size() - failed, failed);
    }
}
