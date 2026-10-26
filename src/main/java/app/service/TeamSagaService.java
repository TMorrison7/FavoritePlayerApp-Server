package app.service;

import app.dto.teamdto.TeamCreationSagaRequest;
import app.dto.teamdto.TeamSagaResponse;
import app.dto.teamdto.TeamSagaStartedEvent;
import app.exception.ResourceNotFoundException;
import app.models.OutboxEvent;
import app.models.TeamSaga;
import app.models.abstractEntities.Player;
import app.repositories.OutboxEventRepository;
import app.repositories.PlayerRepository;
import app.repositories.TeamSagaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamSagaService {

    private final TeamSagaRepository sagaRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PlayerRepository playerRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.team-saga-topic}")
    private String teamSagaTopic;

    @Transactional
    public TeamSagaResponse start(TeamCreationSagaRequest request) {
        List<Long> playerIds = request.playerIds().stream().distinct().toList();
        Set<Long> existingPlayerIds = playerRepository.findAllById(playerIds).stream()
                .map(Player::getId)
                .collect(Collectors.toSet());

        for (Long playerId : playerIds) {
            if (!existingPlayerIds.contains(playerId)) {
                throw new ResourceNotFoundException(String.format("Player with ID: %d not found.", playerId));
            }
        }

        TeamSaga saga = sagaRepository.save(new TeamSaga(request.team(), playerIds));
        try {
            String payload = objectMapper.writeValueAsString(
                    new TeamSagaStartedEvent(UUID.fromString(saga.getId())));
            outboxEventRepository.save(new OutboxEvent(teamSagaTopic, saga.getId(), payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize team saga event", exception);
        }

        return response(saga);
    }

    @Transactional(readOnly = true)
    public TeamSagaResponse getStatus(UUID sagaId) {
        TeamSaga saga = sagaRepository.findById(sagaId.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Team saga not found."));
        return response(saga);
    }

    private TeamSagaResponse response(TeamSaga saga) {
        return new TeamSagaResponse(
                UUID.fromString(saga.getId()),
                saga.getStatus(),
                saga.getTeamId(),
                saga.getFailureMessage()
        );
    }
}
