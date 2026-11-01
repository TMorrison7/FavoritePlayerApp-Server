package app.practice.contracts;

public enum SagaStatus {
    PENDING,
    PROCESSING,
    COMPENSATING,
    COMPLETED,
    FAILED,
    COMPENSATION_FAILED
}
