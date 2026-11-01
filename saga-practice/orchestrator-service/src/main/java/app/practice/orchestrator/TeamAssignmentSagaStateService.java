package app.practice.orchestrator;

import app.practice.contracts.SagaStatus;
import app.practice.contracts.TeamAssignmentSagaRequest;
import app.practice.contracts.TeamAssignmentSagaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TeamAssignmentSagaStateService {

    private final TeamAssignmentSagaRepository repository;

    public TeamAssignmentSagaStateService(TeamAssignmentSagaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String create(TeamAssignmentSagaRequest request) {
        return repository.saveAndFlush(new TeamAssignmentSagaEntity(request)).getId();
    }

    @Transactional(readOnly = true)
    public TeamAssignmentSagaEntity get(String sagaId) {
        return repository.findById(sagaId)
                .orElseThrow(() -> new SagaNotFoundException(sagaId));
    }

    @Transactional(readOnly = true)
    public TeamAssignmentSagaResponse response(String sagaId) {
        return response(get(sagaId));
    }

    @Transactional
    public TeamAssignmentSagaData markProcessing(String sagaId) {
        TeamAssignmentSagaEntity saga = get(sagaId);
        if (saga.getStatus() == SagaStatus.PENDING) {
            saga.setStatus(SagaStatus.PROCESSING);
        }
        return new TeamAssignmentSagaData(
                saga.getId(),
                saga.getStatus(),
                saga.getTeamName(),
                saga.getCity(),
                saga.getState(),
                saga.getDivision(),
                saga.getTeamType(),
                saga.getColors(),
                saga.getPlayerIds(),
                saga.getTeamId()
        );
    }

    @Transactional
    public void saveTeamId(String sagaId, Long teamId) {
        TeamAssignmentSagaEntity saga = get(sagaId);
        saga.setTeamId(teamId);
    }

    @Transactional
    public void markCompleted(String sagaId) {
        TeamAssignmentSagaEntity saga = get(sagaId);
        saga.setStatus(SagaStatus.COMPLETED);
        saga.setFailureMessage(null);
    }

    @Transactional
    public void beginCompensation(String sagaId, String failureMessage) {
        TeamAssignmentSagaEntity saga = get(sagaId);
        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setFailureMessage(failureMessage);
    }

    @Transactional
    public void markFailed(String sagaId) {
        TeamAssignmentSagaEntity saga = get(sagaId);
        saga.setStatus(SagaStatus.FAILED);
    }

    @Transactional
    public void markCompensationFailed(String sagaId, String compensationFailure) {
        TeamAssignmentSagaEntity saga = get(sagaId);
        saga.setStatus(SagaStatus.COMPENSATION_FAILED);
        String originalFailure = saga.getFailureMessage();
        saga.setFailureMessage((originalFailure == null ? "" : originalFailure + " | ")
                + "Compensation failed: " + compensationFailure);
    }

    @Transactional
    public void retryCompensation(String sagaId) {
        TeamAssignmentSagaEntity saga = get(sagaId);
        if (saga.getStatus() != SagaStatus.COMPENSATION_FAILED) {
            throw new IllegalStateException("Saga is not awaiting compensation retry.");
        }
        saga.setStatus(SagaStatus.COMPENSATING);
    }

    @Transactional
    public void prepareResume(String sagaId) {
        TeamAssignmentSagaEntity saga = get(sagaId);
        if (saga.getStatus() != SagaStatus.PROCESSING && saga.getStatus() != SagaStatus.PENDING) {
            throw new IllegalStateException("Only pending or processing sagas can be resumed.");
        }
    }

    private TeamAssignmentSagaResponse response(TeamAssignmentSagaEntity saga) {
        return new TeamAssignmentSagaResponse(
                UUID.fromString(saga.getId()).toString(),
                saga.getStatus(),
                saga.getTeamId(),
                saga.getFailureMessage()
        );
    }
}
