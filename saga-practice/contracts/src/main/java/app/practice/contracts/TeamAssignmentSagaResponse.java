package app.practice.contracts;

public record TeamAssignmentSagaResponse(
        String sagaId,
        SagaStatus status,
        Long teamId,
        String failureMessage
) {
}
