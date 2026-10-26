package app.service;

import app.config.PlayerCacheConfiguration;
import app.models.TeamSaga;
import app.models.TeamSagaPlayerStep;
import app.models.TeamSagaStatus;
import app.models.abstractEntities.Player;
import app.models.abstractEntities.Team;
import app.repositories.PlayerRepository;
import app.repositories.TeamRepository;
import app.repositories.TeamSagaPlayerStepRepository;
import app.repositories.TeamSagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(TeamSagaCacheTest.TestConfiguration.class)
class TeamSagaCacheTest {

    @Autowired
    private TeamSagaStepService stepService;

    @Autowired
    private TeamSagaRepository sagaRepository;

    @Autowired
    private TeamSagaPlayerStepRepository playerStepRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearState() {
        reset(sagaRepository, playerStepRepository, teamRepository, playerRepository);
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    void evictsTeamCacheWhenSagaCreatesTeam() {
        TeamSaga saga = saga(TeamSagaStatus.PROCESSING);
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
        Team createdTeam = mock(Team.class);
        when(createdTeam.getId()).thenReturn(55L);
        when(teamRepository.saveAndFlush(any(Team.class))).thenReturn(createdTeam);
        cacheManager.getCache("teams").put("all", List.of(mock(Team.class)));

        stepService.createTeam(saga.getId());

        assertNull(cacheManager.getCache("teams").get("all"));
    }

    @Test
    void evictsTeamAndPlayerCachesWhenSagaAssignsPlayer() {
        TeamSaga saga = saga(TeamSagaStatus.PROCESSING);
        saga.setTeamId(55L);
        Player player = mock(Player.class);
        Team targetTeam = mock(Team.class);
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
        when(playerStepRepository.existsBySagaIdAndPlayerId(saga.getId(), 10L)).thenReturn(false);
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(teamRepository.findById(55L)).thenReturn(Optional.of(targetTeam));
        cacheManager.getCache("teams").put("all", List.of(mock(Team.class)));
        cacheManager.getCache("players").put("all", List.of(mock(Player.class)));
        cacheManager.getCache("playerSearch").put("search", List.of(mock(Player.class)));

        stepService.assignPlayer(saga.getId(), 10L);

        assertNull(cacheManager.getCache("teams").get("all"));
        assertNull(cacheManager.getCache("players").get("all"));
        assertNull(cacheManager.getCache("playerSearch").get("search"));
    }

    @Test
    void evictsTeamAndPlayerCachesWhenCompensationRestoresPlayer() {
        TeamSaga saga = saga(TeamSagaStatus.COMPENSATING);
        Player player = mock(Player.class);
        Team oldTeam = mock(Team.class);
        Team sagaTeam = mock(Team.class);
        TeamSagaPlayerStep step = new TeamSagaPlayerStep(saga.getId(), 10L, 12L);
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
        when(playerStepRepository.findTopBySagaIdOrderByIdDesc(saga.getId())).thenReturn(Optional.of(step));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(teamRepository.findById(12L)).thenReturn(Optional.of(oldTeam));
        cacheManager.getCache("teams").put("all", List.of(sagaTeam));
        cacheManager.getCache("players").put("all", List.of(player));
        cacheManager.getCache("playerSearch").put("search", List.of(player));

        stepService.compensateNextPlayer(saga.getId());

        assertNull(cacheManager.getCache("teams").get("all"));
        assertNull(cacheManager.getCache("players").get("all"));
        assertNull(cacheManager.getCache("playerSearch").get("search"));
    }

    private TeamSaga saga(TeamSagaStatus status) {
        TeamSaga saga = new TeamSaga(
                new app.dto.teamdto.ProTeamRequestDto("Example", "City", "State", List.of(), "Division"),
                List.of(10L));
        saga.setStatus(status);
        return saga;
    }

    @Configuration
    @Import({PlayerCacheConfiguration.class, TeamSagaStepService.class})
    static class TestConfiguration {

        @Bean
        TeamSagaRepository teamSagaRepository() {
            return mock(TeamSagaRepository.class);
        }

        @Bean
        TeamSagaPlayerStepRepository teamSagaPlayerStepRepository() {
            return mock(TeamSagaPlayerStepRepository.class);
        }

        @Bean
        TeamRepository teamRepository() {
            return mock(TeamRepository.class);
        }

        @Bean
        PlayerRepository playerRepository() {
            return mock(PlayerRepository.class);
        }
    }
}
