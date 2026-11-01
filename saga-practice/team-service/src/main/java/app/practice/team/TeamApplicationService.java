package app.practice.team;

import app.practice.contracts.CreateTeamCommand;
import app.practice.contracts.CreateTeamRequest;
import app.practice.contracts.TeamCreatedResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamApplicationService {

    private final TeamRepository teamRepository;

    public TeamApplicationService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional
    public TeamCreatedResponse create(CreateTeamCommand command) {
        return teamRepository.findBySagaId(command.sagaId())
                .map(this::response)
                .orElseGet(() -> createNew(command));
    }

    @Transactional(readOnly = true)
    public TeamCreatedResponse findBySaga(String sagaId) {
        return teamRepository.findBySagaId(sagaId)
                .map(this::response)
                .orElseThrow(() -> new TeamNotFoundException(sagaId));
    }

    private TeamCreatedResponse createNew(CreateTeamCommand command) {
        CreateTeamRequest request = command.team();
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Team name is required.");
        }
        TeamEntity team = teamRepository.saveAndFlush(new TeamEntity(
                command.sagaId(),
                request.name(),
                request.city(),
                request.state(),
                request.division(),
                request.colors(),
                request.teamType() == null ? "PRO" : request.teamType()
        ));
        return response(team);
    }

    @Transactional
    public void compensateCreate(String sagaId) {
        teamRepository.findBySagaId(sagaId).ifPresent(teamRepository::delete);
    }

    private TeamCreatedResponse response(TeamEntity team) {
        return new TeamCreatedResponse(team.getId(), team.getName(), team.getTeamType());
    }
}
