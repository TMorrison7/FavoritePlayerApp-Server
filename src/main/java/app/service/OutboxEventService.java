package app.service;

import app.models.OutboxEvent;
import app.repositories.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void enqueue(String topic, String messageKey, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxEventRepository.save(new OutboxEvent(topic, messageKey, payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize outbox event", exception);
        }
    }

    @Transactional
    public void markPublished(String eventId) {
        OutboxEvent event = findEvent(eventId);
        event.setPublishedAt(Instant.now());
        event.setAttempts(event.getAttempts() + 1);
        event.setLastError(null);
    }

    @Transactional
    public void recordFailure(String eventId, Exception failure) {
        OutboxEvent event = findEvent(eventId);
        event.setAttempts(event.getAttempts() + 1);
        event.setLastError(truncate(failure.getMessage()));
    }

    private OutboxEvent findEvent(String eventId) {
        return outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException("Outbox event not found: " + eventId));
    }

    private String truncate(String value) {
        return value == null ? null : value.substring(0, Math.min(value.length(), 2000));
    }
}
