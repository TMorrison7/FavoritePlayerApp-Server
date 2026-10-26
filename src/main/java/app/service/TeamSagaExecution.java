package app.service;

import app.models.TeamSagaStatus;

import java.util.List;

public record TeamSagaExecution(TeamSagaStatus status, Long teamId, List<Long> playerIds) {
}
