package app.dto.userdto;

import app.models.FavoriteSagaStatus;

import java.util.UUID;

public record FavoriteSagaResponse(
        UUID sagaId,
        FavoriteSagaStatus status,
        String failureMessage
) {
}
