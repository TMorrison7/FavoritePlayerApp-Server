package app.practice.orchestrator;

import app.practice.contracts.SagaStatus;

import java.util.List;

public record TeamAssignmentSagaData(
        String sagaId,
        SagaStatus status,
        String teamName,
        String city,
        String state,
        String division,
        String teamType,
        List<String> colors,
        List<Long> playerIds,
        Long teamId
) {
}
