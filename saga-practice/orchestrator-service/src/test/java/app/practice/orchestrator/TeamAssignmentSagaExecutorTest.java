package app.practice.orchestrator;

import app.practice.contracts.SagaStatus;
import app.practice.contracts.TeamAssignmentSagaResponse;
import app.practice.contracts.TeamCreatedResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeamAssignmentSagaExecutorTest {

    @Test
    void completesTeamCreationAndPlayerAssignmentsInOrder() {
        TeamAssignmentSagaStateService stateService = mock(TeamAssignmentSagaStateService.class);
        SagaServiceClients clients = mock(SagaServiceClients.class);
        TeamAssignmentSagaExecutor executor = new TeamAssignmentSagaExecutor(stateService, clients);
        TeamAssignmentSagaData saga = saga();
        when(stateService.markProcessing("saga-1")).thenReturn(saga);
        when(clients.createTeam(any())).thenReturn(new TeamCreatedResponse(12L, "Example", "PRO"));
        when(stateService.response("saga-1"))
                .thenReturn(new TeamAssignmentSagaResponse("saga-1", SagaStatus.COMPLETED, 12L, null));

        var response = executor.execute("saga-1");

        assertEquals(SagaStatus.COMPLETED, response.status());
        var order = inOrder(clients, stateService);
        order.verify(clients).createTeam(any());
        order.verify(stateService).saveTeamId("saga-1", 12L);
        order.verify(clients).assignPlayer(argThat(command ->
                command.playerId().equals(5L) && command.stepNumber() == 0));
        order.verify(clients).assignPlayer(argThat(command ->
                command.playerId().equals(6L) && command.stepNumber() == 1));
        order.verify(stateService).markCompleted("saga-1");
        verify(clients, never()).restorePlayers(any());
    }

    @Test
    void compensatesCompletedStepsWhenLaterPlayerAssignmentFails() {
        TeamAssignmentSagaStateService stateService = mock(TeamAssignmentSagaStateService.class);
        SagaServiceClients clients = mock(SagaServiceClients.class);
        TeamAssignmentSagaExecutor executor = new TeamAssignmentSagaExecutor(stateService, clients);
        TeamAssignmentSagaData saga = saga();
        when(stateService.markProcessing("saga-1")).thenReturn(saga);
        when(clients.createTeam(any())).thenReturn(new TeamCreatedResponse(12L, "Example", "PRO"));
        doThrow(new IllegalArgumentException("Unknown player"))
                .when(clients).assignPlayer(argThat(command ->
                        command.playerId().equals(6L) && command.stepNumber() == 1));
        when(stateService.response("saga-1"))
                .thenReturn(new TeamAssignmentSagaResponse("saga-1", SagaStatus.FAILED, null, "Unknown player"));

        var response = executor.execute("saga-1");

        assertEquals(SagaStatus.FAILED, response.status());
        var order = inOrder(clients, stateService);
        order.verify(clients).assignPlayer(argThat(command ->
                command.playerId().equals(5L) && command.stepNumber() == 0));
        order.verify(clients).assignPlayer(argThat(command ->
                command.playerId().equals(6L) && command.stepNumber() == 1));
        order.verify(stateService).beginCompensation("saga-1", "Unknown player");
        order.verify(clients).restorePlayers("saga-1");
        order.verify(clients).deleteTeamForSaga("saga-1");
        order.verify(stateService).markFailed("saga-1");
    }

    private TeamAssignmentSagaData saga() {
        return new TeamAssignmentSagaData(
                "saga-1",
                SagaStatus.PROCESSING,
                "Example",
                "City",
                "CA",
                "Division",
                "PRO",
                List.of("blue"),
                List.of(5L, 6L),
                null);
    }
}
