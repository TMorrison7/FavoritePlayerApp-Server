package app.service;

import app.models.TeamSagaStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TeamSagaProcessorTest {

    private TeamSagaStepService stepService;
    private TeamSagaProcessor processor;

    @BeforeEach
    void setUp() {
        stepService = mock(TeamSagaStepService.class);
        processor = new TeamSagaProcessor(stepService, new ObjectMapper());
    }

    @Test
    void createsTeamAssignsPlayersAndCompletesSaga() {
        UUID sagaId = UUID.randomUUID();
        when(stepService.begin(sagaId.toString()))
                .thenReturn(new TeamSagaExecution(TeamSagaStatus.PROCESSING, null, List.of(10L, 20L)));
        when(stepService.createTeam(sagaId.toString())).thenReturn(55L);

        processor.process(sagaId);

        InOrder calls = inOrder(stepService);
        calls.verify(stepService).createTeam(sagaId.toString());
        calls.verify(stepService).assignPlayer(sagaId.toString(), 10L);
        calls.verify(stepService).assignPlayer(sagaId.toString(), 20L);
        calls.verify(stepService).complete(sagaId.toString());
    }

    @Test
    void resumesExistingTeamWithoutCreatingItAgain() {
        UUID sagaId = UUID.randomUUID();
        when(stepService.begin(sagaId.toString()))
                .thenReturn(new TeamSagaExecution(TeamSagaStatus.PROCESSING, 55L, List.of(10L)));

        processor.process(sagaId);

        verify(stepService, never()).createTeam(anyString());
        verify(stepService).assignPlayer(sagaId.toString(), 10L);
        verify(stepService).complete(sagaId.toString());
    }

    @Test
    void compensatesAssignmentsAndCreatedTeamAfterStepFailure() {
        UUID sagaId = UUID.randomUUID();
        when(stepService.begin(sagaId.toString()))
                .thenReturn(new TeamSagaExecution(TeamSagaStatus.PROCESSING, 55L, List.of(10L, 20L)));
        when(stepService.beginCompensation(anyString(), anyString())).thenReturn(true);
        doThrow(new IllegalStateException("assignment failed"))
                .when(stepService).assignPlayer(sagaId.toString(), 20L);
        when(stepService.compensateNextPlayer(sagaId.toString())).thenReturn(10L, (Long) null);

        processor.process(sagaId);

        verify(stepService).beginCompensation(eq(sagaId.toString()), anyString());
        verify(stepService, times(2)).compensateNextPlayer(sagaId.toString());
        verify(stepService).finishCompensation(sagaId.toString());
        verify(stepService, never()).complete(sagaId.toString());
    }

    @Test
    void resumesCompensationOnRedeliveredEvent() {
        UUID sagaId = UUID.randomUUID();
        when(stepService.begin(sagaId.toString()))
                .thenReturn(new TeamSagaExecution(TeamSagaStatus.COMPENSATING, 55L, List.of(10L)));
        when(stepService.compensateNextPlayer(sagaId.toString())).thenReturn(10L, (Long) null);

        processor.process(sagaId);

        verify(stepService, never()).assignPlayer(anyString(), anyLong());
        verify(stepService).finishCompensation(sagaId.toString());
    }

    @Test
    void ignoresDuplicateEventForTerminalSaga() {
        UUID sagaId = UUID.randomUUID();
        when(stepService.begin(sagaId.toString()))
                .thenReturn(new TeamSagaExecution(TeamSagaStatus.COMPLETED, 55L, List.of(10L)));

        processor.process(sagaId);

        verify(stepService, never()).createTeam(anyString());
        verify(stepService, never()).assignPlayer(anyString(), anyLong());
        verify(stepService, never()).complete(anyString());
    }

    @Test
    void rejectsMalformedKafkaPayload() {
        assertThrows(IllegalArgumentException.class, () -> processor.handle("not-json"));
    }
}
