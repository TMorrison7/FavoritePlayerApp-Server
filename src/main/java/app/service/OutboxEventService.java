package app.service;

import app.models.OutboxEvent;
import app.repositories.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

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
