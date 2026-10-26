package app.service;

import app.dto.teamdto.TeamSagaStartedEvent;
import app.models.TeamSagaStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TeamSagaProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TeamSagaProcessor.class);

    private final TeamSagaStepService stepService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.team-saga-topic}")
    public void handle(String payload) {
        TeamSagaStartedEvent event;
        try {
            event = objectMapper.readValue(payload, TeamSagaStartedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid team saga event", exception);
        }
        process(event.sagaId());
    }

    void process(UUID sagaId) {
        TeamSagaExecution saga = stepService.begin(sagaId.toString());
        if (saga.status() == TeamSagaStatus.COMPLETED || saga.status() == TeamSagaStatus.FAILED) {
            return;
        }
        if (saga.status() == TeamSagaStatus.COMPENSATING) {
            compensate(sagaId.toString());
            return;
        }

        try {
            if (saga.teamId() == null) {
                stepService.createTeam(sagaId.toString());
            }
            for (Long playerId : saga.playerIds()) {
                stepService.assignPlayer(sagaId.toString(), playerId);
            }
            stepService.complete(sagaId.toString());
        } catch (RuntimeException failure) {
            logger.error("Team saga {} failed; starting compensation", sagaId, failure);
            boolean compensationStarted = stepService.beginCompensation(
                    sagaId.toString(),
                    "Team creation or player assignment failed; changes are being rolled back.");
            if (compensationStarted) {
                compensate(sagaId.toString());
            }
        }
    }

    private void compensate(String sagaId) {
        while (stepService.compensateNextPlayer(sagaId) != null) {
            logger.info("Restored a previous player-team assignment for saga {}", sagaId);
        }
        stepService.finishCompensation(sagaId);
    }
}
