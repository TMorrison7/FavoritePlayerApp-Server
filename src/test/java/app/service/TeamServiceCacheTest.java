package app.service;

import app.config.PlayerCacheConfiguration;
import app.dto.teamdto.ProTeamRequest;
import app.dto.teamdto.TeamRequest;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
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
        reset(teamRepository, validator);
        when(validator.validate(any(TeamRequest.class))).thenReturn(Set.of());
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
    void cachesSearchByRequestValue() {
        List<Team> teams = List.of(mock(Team.class));
        when(teamRepository.findAll(any(Specification.class))).thenReturn(teams);
        ProTeamRequest request = new ProTeamRequest();
        request.setName("Warriors");
        ProTeamRequest equivalentRequest = new ProTeamRequest();
        equivalentRequest.setName("Warriors");

        assertSame(teams, teamService.search(request));
        assertSame(teams, teamService.search(equivalentRequest));

        verify(teamRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void evictsTeamCachesAfterSuccessfulCreate() {
        List<Team> cachedTeams = List.of(mock(Team.class));
        List<Team> reloadedTeams = List.of(mock(Team.class));
        when(teamRepository.findAll()).thenReturn(cachedTeams, reloadedTeams);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertSame(cachedTeams, teamService.getAllTeams());
        teamService.create(proTeamRequest());
        assertSame(reloadedTeams, teamService.getAllTeams());

        verify(teamRepository, times(2)).findAll();
    }

    private ProTeamRequest proTeamRequest() {
        ProTeamRequest request = new ProTeamRequest();
        request.setName("New team");
        request.setCity("City");
        request.setState("State");
        return request;
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
