package app.service;

import app.dto.events.PlayerCreatedEvent;
import app.dto.events.TeamCreatedEvent;
import app.dto.playerdto.BasketballRequest;
import app.dto.teamdto.ProTeamRequest;
import app.models.abstractEntities.Player;
import app.models.abstractEntities.Team;
import app.models.entities.ProTeam;
import app.repositories.PlayerRepository;
import app.repositories.TeamRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreationOutboxEventTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private Validator validator;

    @Mock
    private OutboxEventService outboxEventService;

    private TeamService teamService;
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        teamService = new TeamService(teamRepository, validator, outboxEventService);
        playerService = new PlayerService(playerRepository, validator, outboxEventService);
        ReflectionTestUtils.setField(teamService, "teamCreatedTopic", "team-created-events");
        ReflectionTestUtils.setField(playerService, "playerCreatedTopic", "player-created-events");
        when(validator.validate(any())).thenReturn(Set.of());
    }

    @Test
    void enqueuesTeamCreatedEventWithSavedTeamId() {
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            team.setId(21L);
            return team;
        });
        ProTeamRequest request = new ProTeamRequest();
        request.setName("Warriors");
        request.setCity("San Francisco");
        request.setState("CA");
        request.setDivision("NBA");
        request.setColors(List.of("blue", "gold"));

        teamService.create(request);

        verify(outboxEventService).enqueue(
                eq("team-created-events"),
                eq("21"),
                eq(new TeamCreatedEvent(21L, "Warriors", "San Francisco", "CA", "NBA", "PRO")));
    }

    @Test
    void enqueuesPlayerCreatedEventWithSavedPlayerIdAndTeam() {
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player player = invocation.getArgument(0);
            player.setId(42L);
            return player;
        });
        BasketballRequest request = new BasketballRequest();
        request.setFirstName("Stephen");
        request.setLastName("Curry");
        request.setTeam(ProTeam.builder().id(21L).name("Warriors").build());

        playerService.create(request);

        verify(outboxEventService).enqueue(
                eq("player-created-events"),
                eq("42"),
                eq(new PlayerCreatedEvent(42L, "Stephen", "Curry", "Basketball", 21L)));
    }
}
