package app.service;

import app.models.OutboxEvent;
import app.repositories.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class OutboxPublisherTest {

    @Test
    void publishesPendingEventAndMarksItPublished() {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        OutboxEventService eventService = mock(OutboxEventService.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        OutboxEvent event = new OutboxEvent("player-created-events", "42", "{\"playerId\":42}");
        when(repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(event));
        when(kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload()))
                .thenReturn(CompletableFuture.completedFuture(null));
        OutboxPublisher publisher = new OutboxPublisher(repository, eventService, kafkaTemplate);
        ReflectionTestUtils.setField(publisher, "publishTimeoutSeconds", 1L);

        publisher.publishPendingEvents();

        verify(kafkaTemplate).send(event.getTopic(), event.getMessageKey(), event.getPayload());
        verify(eventService).markPublished(event.getId());
        verify(eventService, never()).recordFailure(eq(event.getId()), any());
    }
}
