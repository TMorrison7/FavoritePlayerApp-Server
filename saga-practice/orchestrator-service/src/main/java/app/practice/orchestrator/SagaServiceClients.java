package app.practice.orchestrator;

import app.practice.contracts.AssignPlayerCommand;
import app.practice.contracts.CreateTeamCommand;
import app.practice.contracts.TeamCreatedResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SagaServiceClients {

    private final RestClient teamClient;
    private final RestClient playerClient;

    public SagaServiceClients(
            RestClient.Builder builder,
            @Value("${saga.services.team-url}") String teamUrl,
            @Value("${saga.services.player-url}") String playerUrl) {
        this.teamClient = builder.clone().baseUrl(teamUrl).build();
        this.playerClient = builder.clone().baseUrl(playerUrl).build();
    }

    public TeamCreatedResponse createTeam(CreateTeamCommand command) {
        return teamClient.post()
                .uri("/internal/teams")
                .body(command)
                .retrieve()
                .body(TeamCreatedResponse.class);
    }

    public void deleteTeamForSaga(String sagaId) {
        teamClient.delete()
                .uri("/internal/teams/by-saga/{sagaId}", sagaId)
                .retrieve()
                .toBodilessEntity();
    }

    public void assignPlayer(AssignPlayerCommand command) {
        playerClient.post()
                .uri("/internal/player-assignments")
                .body(command)
                .retrieve()
                .toBodilessEntity();
    }

    public void restorePlayers(String sagaId) {
        playerClient.post()
                .uri("/internal/player-assignments/{sagaId}/compensate", sagaId)
                .retrieve()
                .toBodilessEntity();
    }
}
