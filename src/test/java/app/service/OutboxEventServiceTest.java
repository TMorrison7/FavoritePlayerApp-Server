package app.service;

import app.dto.events.PlayerCreatedEvent;
import app.models.OutboxEvent;
import app.repositories.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxEventServiceTest {

    @Test
    void serializesAndPersistsDomainEvent() throws Exception {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        OutboxEventService service = new OutboxEventService(repository, new ObjectMapper());
        PlayerCreatedEvent payload = new PlayerCreatedEvent(42L, "Stephen", "Curry", "Basketball", 21L);

        service.enqueue("player-created-events", "42", payload);

        var eventCaptor = org.mockito.ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(eventCaptor.capture());
        OutboxEvent savedEvent = eventCaptor.getValue();
        assertEquals("player-created-events", savedEvent.getTopic());
        assertEquals("42", savedEvent.getMessageKey());
        assertEquals(payload, new ObjectMapper().readValue(savedEvent.getPayload(), PlayerCreatedEvent.class));
    }

    @Test
    void failsClearlyWhenEventCannotBeSerialized() throws Exception {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any())).thenThrow(
                new com.fasterxml.jackson.core.JsonProcessingException("serialization failed") { });
        OutboxEventService service = new OutboxEventService(repository, objectMapper);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.enqueue("topic", "key", new Object()));
        verifyNoInteractions(repository);
    }
}
