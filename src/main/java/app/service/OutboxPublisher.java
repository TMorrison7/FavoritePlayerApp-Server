package app.service;

import app.models.OutboxEvent;
import app.repositories.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventService outboxEventService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.outbox.publish-timeout-seconds:10}")
    private long publishTimeoutSeconds;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    public void publishPendingEvents() {
        for (OutboxEvent event : outboxEventRepository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload())
                        .get(publishTimeoutSeconds, TimeUnit.SECONDS);
                outboxEventService.markPublished(event.getId());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException | TimeoutException | RuntimeException exception) {
                logger.error("Failed to publish outbox event {}", event.getId(), exception);
                outboxEventService.recordFailure(event.getId(), exception);
            }
        }
    }
}
