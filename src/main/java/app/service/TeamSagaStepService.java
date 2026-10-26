package app.service;

import app.exception.ResourceNotFoundException;
import app.models.TeamSaga;
import app.models.TeamSagaPlayerStep;
import app.models.TeamSagaStatus;
import app.models.abstractEntities.Player;
import app.models.abstractEntities.Team;
import app.models.entities.College;
import app.models.entities.ProTeam;
import app.repositories.PlayerRepository;
import app.repositories.TeamRepository;
import app.repositories.TeamSagaPlayerStepRepository;
import app.repositories.TeamSagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamSagaStepService {

    private final TeamSagaRepository sagaRepository;
    private final TeamSagaPlayerStepRepository playerStepRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public TeamSagaExecution begin(String sagaId) {
        TeamSaga saga = getSaga(sagaId);
        if (saga.getStatus() == TeamSagaStatus.PENDING) {
            saga.setStatus(TeamSagaStatus.PROCESSING);
        }
        return new TeamSagaExecution(saga.getStatus(), saga.getTeamId(), List.copyOf(saga.getPlayerIds()));
    }

    @Transactional
    @CacheEvict(cacheNames = "teams", allEntries = true)
    public Long createTeam(String sagaId) {
        TeamSaga saga = getSaga(sagaId);
        if (saga.getStatus() != TeamSagaStatus.PROCESSING) {
            return saga.getTeamId();
        }
        if (saga.getTeamId() != null) {
            return saga.getTeamId();
        }

        Team team;
        if ("COLLEGE".equals(saga.getTeamType())) {
            team = College.builder()
                    .name(saga.getTeamName())
                    .city(saga.getCity())
                    .state(saga.getState())
                    .colors(saga.getColors())
                    .division(saga.getDivision())
                    .build();
        } else {
            team = ProTeam.builder()
                    .name(saga.getTeamName())
                    .city(saga.getCity())
                    .state(saga.getState())
                    .colors(saga.getColors())
                    .division(saga.getDivision())
                    .build();
        }

        saga.setTeamId(teamRepository.saveAndFlush(team).getId());
        return saga.getTeamId();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "teams", allEntries = true),
            @CacheEvict(cacheNames = "players", allEntries = true),
            @CacheEvict(cacheNames = "playerSearch", allEntries = true)
    })
    public void assignPlayer(String sagaId, Long playerId) {
        TeamSaga saga = getSaga(sagaId);
        if (saga.getStatus() != TeamSagaStatus.PROCESSING
                || playerStepRepository.existsBySagaIdAndPlayerId(sagaId, playerId)) {
            return;
        }
        if (saga.getTeamId() == null) {
            throw new IllegalStateException("Team must be created before assigning players.");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Player with ID: %d not found.", playerId)));
        Team targetTeam = teamRepository.findById(saga.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Team with ID: %d not found.", saga.getTeamId())));
        Long previousTeamId = player.getTeam() == null ? null : player.getTeam().getId();
        player.setTeam(targetTeam);
        playerRepository.save(player);
        playerStepRepository.saveAndFlush(new TeamSagaPlayerStep(sagaId, playerId, previousTeamId));
    }

    @Transactional
    public boolean beginCompensation(String sagaId, String failureMessage) {
        TeamSaga saga = getSaga(sagaId);
        if (saga.getStatus() != TeamSagaStatus.PROCESSING) {
            return false;
        }
        saga.setFailureMessage(failureMessage == null
                ? null
                : failureMessage.substring(0, Math.min(failureMessage.length(), 2000)));
        saga.setStatus(TeamSagaStatus.COMPENSATING);
        return true;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "teams", allEntries = true),
            @CacheEvict(cacheNames = "players", allEntries = true),
            @CacheEvict(cacheNames = "playerSearch", allEntries = true)
    })
    public Long compensateNextPlayer(String sagaId) {
        TeamSaga saga = getSaga(sagaId);
        if (saga.getStatus() != TeamSagaStatus.COMPENSATING) {
            return null;
        }
        TeamSagaPlayerStep step = playerStepRepository.findTopBySagaIdOrderByIdDesc(sagaId)
                .orElse(null);
        if (step == null) {
            return null;
        }

        Player player = playerRepository.findById(step.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Player with ID: %d not found.", step.getPlayerId())));
        Team previousTeam = step.getPreviousTeamId() == null
                ? null
                : teamRepository.findById(step.getPreviousTeamId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                String.format("Team with ID: %d not found.", step.getPreviousTeamId())));
        player.setTeam(previousTeam);
        playerRepository.save(player);
        playerStepRepository.delete(step);
        return step.getPlayerId();
    }

    @Transactional
    public void complete(String sagaId) {
        TeamSaga saga = getSaga(sagaId);
        if (saga.getStatus() == TeamSagaStatus.PROCESSING) {
            saga.setStatus(TeamSagaStatus.COMPLETED);
            saga.setFailureMessage(null);
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "teams", allEntries = true)
    public void finishCompensation(String sagaId) {
        TeamSaga saga = getSaga(sagaId);
        if (saga.getStatus() != TeamSagaStatus.COMPENSATING
                || playerStepRepository.findTopBySagaIdOrderByIdDesc(sagaId).isPresent()) {
            return;
        }
        if (saga.getTeamId() != null) {
            teamRepository.deleteById(saga.getTeamId());
            saga.setTeamId(null);
        }
        saga.setStatus(TeamSagaStatus.FAILED);
    }

    private TeamSaga getSaga(String sagaId) {
        return sagaRepository.findById(sagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Team saga not found: " + sagaId));
    }
}
