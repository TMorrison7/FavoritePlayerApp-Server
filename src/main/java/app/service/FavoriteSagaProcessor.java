package app.service;

import app.dto.userdto.FavoriteSagaStartedEvent;
import app.models.FavoriteSaga;
import app.models.FavoriteSagaStatus;
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
public class FavoriteSagaProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteSagaProcessor.class);

    private final FavoriteSagaStepService stepService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.favorite-saga-topic}")
    public void handle(String payload) {
        FavoriteSagaStartedEvent event;
        try {
            event = objectMapper.readValue(payload, FavoriteSagaStartedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid favorite saga event", exception);
        }
        process(event.sagaId());
    }

    void process(UUID sagaId) {
        FavoriteSagaExecution saga = stepService.begin(sagaId.toString());
        if (saga.status() == FavoriteSagaStatus.COMPLETED
                || saga.status() == FavoriteSagaStatus.FAILED) {
            return;
        }
        if (saga.status() == FavoriteSagaStatus.COMPENSATING) {
            compensate(sagaId.toString());
            return;
        }

        try {
            for (Long playerId : saga.playerIds()) {
                stepService.addFavorite(sagaId.toString(), playerId);
            }
            stepService.complete(sagaId.toString());
        } catch (RuntimeException failure) {
            logger.error("Favorite saga {} failed; starting compensation", sagaId, failure);
            boolean compensationStarted = stepService.beginCompensation(
                    sagaId.toString(),
                    "Favorite operation failed; newly added favorites are being rolled back.");
            if (compensationStarted) {
                compensate(sagaId.toString());
            }
        }
    }

    private void compensate(String sagaId) {
        while (stepService.compensateNext(sagaId) != null) {
            logger.info("Compensated favorite step for saga {}", sagaId);
        }
        stepService.fail(sagaId);
    }
}
