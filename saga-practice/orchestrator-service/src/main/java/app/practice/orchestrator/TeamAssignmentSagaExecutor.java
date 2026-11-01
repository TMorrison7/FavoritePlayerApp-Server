package app.practice.orchestrator;

import app.practice.contracts.AssignPlayerCommand;
import app.practice.contracts.CreateTeamCommand;
import app.practice.contracts.CreateTeamRequest;
import app.practice.contracts.SagaStatus;
import app.practice.contracts.TeamAssignmentSagaResponse;
import app.practice.contracts.TeamCreatedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TeamAssignmentSagaExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TeamAssignmentSagaExecutor.class);

    private final TeamAssignmentSagaStateService stateService;
    private final SagaServiceClients clients;

    public TeamAssignmentSagaExecutor(TeamAssignmentSagaStateService stateService, SagaServiceClients clients) {
        this.stateService = stateService;
        this.clients = clients;
    }

    public TeamAssignmentSagaResponse execute(String sagaId) {
        TeamAssignmentSagaData saga = stateService.markProcessing(sagaId);
        if (saga.status() == SagaStatus.COMPLETED
                || saga.status() == SagaStatus.FAILED
                || saga.status() == SagaStatus.COMPENSATION_FAILED) {
            return stateService.response(sagaId);
        }
        if (saga.status() == SagaStatus.COMPENSATING) {
            return compensate(sagaId);
        }

        try {
            TeamCreatedResponse team = clients.createTeam(new CreateTeamCommand(
                    sagaId,
                    new CreateTeamRequest(
                            saga.teamName(),
                            saga.city(),
                            saga.state(),
                            saga.division(),
                            saga.colors(),
                            saga.teamType())));
            stateService.saveTeamId(sagaId, team.teamId());

            for (int stepNumber = 0; stepNumber < saga.playerIds().size(); stepNumber++) {
                clients.assignPlayer(new AssignPlayerCommand(
                        sagaId, team.teamId(), saga.playerIds().get(stepNumber), stepNumber));
            }
            stateService.markCompleted(sagaId);
        } catch (RuntimeException failure) {
            logger.error("Saga {} failed; starting compensations", sagaId, failure);
            stateService.beginCompensation(sagaId, failure.getMessage());
            return compensate(sagaId);
        }
        return stateService.response(sagaId);
    }

    public TeamAssignmentSagaResponse retryCompensation(String sagaId) {
        stateService.retryCompensation(sagaId);
        return compensate(sagaId);
    }

    private TeamAssignmentSagaResponse compensate(String sagaId) {
        try {
            clients.restorePlayers(sagaId);
            clients.deleteTeamForSaga(sagaId);
            stateService.markFailed(sagaId);
        } catch (RuntimeException compensationFailure) {
            logger.error("Saga {} compensation failed", sagaId, compensationFailure);
            stateService.markCompensationFailed(sagaId, compensationFailure.getMessage());
        }
        return stateService.response(sagaId);
    }
}
