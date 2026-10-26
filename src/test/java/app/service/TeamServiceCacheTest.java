package app.service;

import app.config.PlayerCacheConfiguration;
import app.dto.teamdto.ProTeamRequestDto;
import app.models.abstractEntities.Team;
import app.repositories.TeamRepository;
import jakarta.validation.Validator;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(TeamServiceCacheTest.TestConfiguration.class)
class TeamServiceCacheTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private Validator validator;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearState() {
        reset(teamRepository);
        reset(validator);
        doAnswer(invocation -> Set.of()).when(validator).validate(any(Team.class));
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    void cachesAllTeams() {
        List<Team> teams = List.of(mock(Team.class));
        when(teamRepository.findAll()).thenReturn(teams);

        assertSame(teams, teamService.getAllTeams());
        assertSame(teams, teamService.getAllTeams());

        verify(teamRepository, times(1)).findAll();
    }

    @Test
    void cachesTeamById() {
        Team team = mock(Team.class);
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        assertSame(team, teamService.getTeamById(10L));
        assertSame(team, teamService.getTeamById(10L));

        verify(teamRepository, times(1)).findById(10L);
    }

    @Test
    void cachesTeamByName() {
        Team team = mock(Team.class);
        when(teamRepository.getTeamByName("Warriors")).thenReturn(Optional.of(team));

        assertSame(team, teamService.getTeamByName("Warriors"));
        assertSame(team, teamService.getTeamByName("Warriors"));

        verify(teamRepository, times(1)).getTeamByName("Warriors");
    }

    @Test
    void evictsTeamCachesAfterSuccessfulCreate() {
        Team cachedTeam = mock(Team.class);
        Team savedTeam = mock(Team.class);
        Team reloadedTeam = mock(Team.class);
        List<Team> cachedTeams = List.of(cachedTeam);
        List<Team> reloadedTeams = List.of(reloadedTeam);
        when(teamRepository.findAll()).thenReturn(cachedTeams, reloadedTeams);
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);

        assertSame(cachedTeams, teamService.getAllTeams());
        teamService.createTeam(new ProTeamRequestDto("New team", "City", "State", List.of("blue"), "Division"));
        assertSame(reloadedTeams, teamService.getAllTeams());

        verify(teamRepository, times(2)).findAll();
    }

    @Configuration
    @Import({PlayerCacheConfiguration.class, TeamService.class})
    static class TestConfiguration {

        @Bean
        TeamRepository teamRepository() {
            return mock(TeamRepository.class);
        }

        @Bean
        Validator validator() {
            return mock(Validator.class);
        }
    }
}
