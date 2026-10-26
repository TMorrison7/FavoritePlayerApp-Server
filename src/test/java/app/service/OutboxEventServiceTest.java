package app.service;

import app.models.OutboxEvent;
import app.repositories.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxEventServiceTest {

    private OutboxEventRepository repository;
    private OutboxEventService service;

    @BeforeEach
    void setUp() {
        repository = mock(OutboxEventRepository.class);
        service = new OutboxEventService(repository);
    }

    @Test
    void marksEventPublishedAndClearsLastError() {
        OutboxEvent event = new OutboxEvent("topic", "key", "{}");
        event.setLastError("previous failure");
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        service.markPublished(event.getId());

        assertNotNull(event.getPublishedAt());
        assertEquals(1, event.getAttempts());
        assertNull(event.getLastError());
    }

    @Test
    void recordsPublishFailureAndLeavesEventPending() {
        OutboxEvent event = new OutboxEvent("topic", "key", "{}");
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        service.recordFailure(event.getId(), new IllegalStateException("broker unavailable"));

        assertNull(event.getPublishedAt());
        assertEquals(1, event.getAttempts());
        assertEquals("broker unavailable", event.getLastError());
    }

    @Test
    void reportsMissingOutboxEvent() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.markPublished("missing"));
    }
}
