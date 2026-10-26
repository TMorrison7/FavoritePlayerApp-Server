package app.service;

import app.models.FavoriteSagaStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FavoriteSagaProcessorTest {

    private FavoriteSagaStepService stepService;
    private FavoriteSagaProcessor processor;

    @BeforeEach
    void setUp() {
        stepService = mock(FavoriteSagaStepService.class);
        processor = new FavoriteSagaProcessor(stepService, new ObjectMapper());
    }

    @Test
    void processesEachStepAndMarksSagaComplete() {
        UUID sagaId = UUID.randomUUID();
        when(stepService.begin(sagaId.toString()))
                .thenReturn(new FavoriteSagaExecution(FavoriteSagaStatus.PROCESSING, List.of(10L, 20L)));

        processor.process(sagaId);

        InOrder calls = inOrder(stepService);
        calls.verify(stepService).addFavorite(sagaId.toString(), 10L);
        calls.verify(stepService).addFavorite(sagaId.toString(), 20L);
        calls.verify(stepService).complete(sagaId.toString());
        verify(stepService, never()).beginCompensation(anyString(), anyString());
    }

    @Test
    void compensatesAfterAPlayerStepFailsAndMarksSagaFailed() {
        UUID sagaId = UUID.randomUUID();
        when(stepService.begin(sagaId.toString()))
                .thenReturn(new FavoriteSagaExecution(FavoriteSagaStatus.PROCESSING, List.of(10L, 20L)));
        when(stepService.beginCompensation(sagaId.toString(),
                "Favorite operation failed; newly added favorites are being rolled back.")).thenReturn(true);
        doThrow(new IllegalStateException("second step failed"))
                .when(stepService).addFavorite(sagaId.toString(), 20L);
        when(stepService.compensateNext(sagaId.toString())).thenReturn(10L, (Long) null);

        processor.process(sagaId);

        verify(stepService).beginCompensation(sagaId.toString(),
                "Favorite operation failed; newly added favorites are being rolled back.");
        verify(stepService, times(2)).compensateNext(sagaId.toString());
        verify(stepService).fail(sagaId.toString());
        verify(stepService, never()).complete(sagaId.toString());
    }

    @Test
    void resumesCompensationForRedeliveredEvent() {
        UUID sagaId = UUID.randomUUID();
        when(stepService.begin(sagaId.toString()))
                .thenReturn(new FavoriteSagaExecution(FavoriteSagaStatus.COMPENSATING, List.of(10L)));
        when(stepService.compensateNext(sagaId.toString())).thenReturn(10L, (Long) null);

        processor.process(sagaId);

        verify(stepService, never()).addFavorite(anyString(), anyLong());
        verify(stepService).fail(sagaId.toString());
    }

    @Test
    void ignoresDuplicateEventForTerminalSaga() {
        UUID sagaId = UUID.randomUUID();
        when(stepService.begin(sagaId.toString()))
                .thenReturn(new FavoriteSagaExecution(FavoriteSagaStatus.COMPLETED, List.of(10L)));

        processor.process(sagaId);

        verify(stepService, never()).addFavorite(anyString(), anyLong());
        verify(stepService, never()).complete(anyString());
        verify(stepService, never()).compensateNext(anyString());
    }

    @Test
    void rejectsMalformedKafkaPayload() {
        assertThrows(IllegalArgumentException.class, () -> processor.handle("not-json"));
    }

}
