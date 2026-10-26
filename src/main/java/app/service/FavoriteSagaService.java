package app.service;

import app.dto.userdto.FavoriteSagaResponse;
import app.dto.userdto.FavoriteSagaStartedEvent;
import app.exception.ResourceNotFoundException;
import app.models.FavoriteSaga;
import app.models.OutboxEvent;
import app.models.abstractEntities.Player;
import app.repositories.FavoriteSagaRepository;
import app.repositories.OutboxEventRepository;
import app.repositories.PlayerRepository;
import app.repositories.UserRepository;
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
public class FavoriteSagaService {

    private final FavoriteSagaRepository sagaRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.favorite-saga-topic}")
    private String favoriteSagaTopic;

    @Transactional
    public FavoriteSagaResponse start(String username, List<Long> requestedPlayerIds) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        List<Long> playerIds = requestedPlayerIds.stream().distinct().toList();
        Set<Long> existingPlayerIds = playerRepository.findAllById(playerIds).stream()
                .map(Player::getId)
                .collect(Collectors.toSet());
        for (Long playerId : playerIds) {
            if (!existingPlayerIds.contains(playerId)) {
                throw new ResourceNotFoundException(String.format("Player with ID: %d not found.", playerId));
            }
        }

        FavoriteSaga saga = sagaRepository.save(new FavoriteSaga(username, playerIds));
        try {
            String payload = objectMapper.writeValueAsString(
                    new FavoriteSagaStartedEvent(UUID.fromString(saga.getId())));
            outboxEventRepository.save(new OutboxEvent(favoriteSagaTopic, saga.getId(), payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize favorite saga event", exception);
        }

        return toResponse(saga);
    }

    @Transactional(readOnly = true)
    public FavoriteSagaResponse getStatus(String username, UUID sagaId) {
        FavoriteSaga saga = sagaRepository.findByIdAndUsername(sagaId.toString(), username)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite saga not found."));
        return toResponse(saga);
    }

    private FavoriteSagaResponse toResponse(FavoriteSaga saga) {
        return new FavoriteSagaResponse(
                UUID.fromString(saga.getId()),
                saga.getStatus(),
                saga.getFailureMessage()
        );
    }
}
