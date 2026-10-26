package app.dto.teamdto;

import app.models.TeamSagaStatus;

import java.util.UUID;

public record TeamSagaResponse(
        UUID sagaId,
        TeamSagaStatus status,
        Long teamId,
        String failureMessage
) {
}
