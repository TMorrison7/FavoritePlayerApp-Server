package app.service;

import app.models.FavoriteSagaStatus;

import java.util.List;

public record FavoriteSagaExecution(FavoriteSagaStatus status, List<Long> playerIds) {
}
