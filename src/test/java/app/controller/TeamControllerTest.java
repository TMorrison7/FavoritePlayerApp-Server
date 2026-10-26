package app.controller;

import app.dto.teamdto.ProTeamRequest;
import app.dto.teamdto.TeamCreationSagaRequest;
import app.dto.teamdto.TeamSagaResponse;
import app.models.TeamSagaStatus;
import app.models.abstractEntities.Team;
import app.service.TeamSagaService;
import app.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class TeamControllerTest {

    private TeamService teamService;
    private TeamSagaService teamSagaService;
    private TeamController controller;
    private HttpServletRequest servletRequest;

    @BeforeEach
    void setUp() {
        teamService = mock(TeamService.class);
        teamSagaService = mock(TeamSagaService.class);
        servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getContextPath()).thenReturn("/favorite-player");
        controller = new TeamController(teamService, teamSagaService);
    }

    @Test
    void createsTeamSagaAndReturnsAcceptedStatusLocation() {
        TeamCreationSagaRequest request = new TeamCreationSagaRequest(
                proTeamRequest("Example", "City", "State", List.of("blue"), "Division"),
                List.of(10L));
        TeamSagaResponse saga = new TeamSagaResponse(
                UUID.randomUUID(), TeamSagaStatus.PENDING, null, null);
        when(teamSagaService.start(request)).thenReturn(saga);

        var response = controller.createTeamSaga(request, servletRequest);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(saga, response.getBody());
        assertEquals("/favorite-player/teams/sagas/" + saga.sagaId(),
                response.getHeaders().getFirst("Location"));
        verify(teamSagaService).start(request);
        verifyNoInteractions(teamService);
    }

    @Test
    void readsTeamSagaStatus() {
        UUID sagaId = UUID.randomUUID();
        TeamSagaResponse saga = new TeamSagaResponse(sagaId, TeamSagaStatus.COMPLETED, 55L, null);
        when(teamSagaService.getStatus(sagaId)).thenReturn(saga);

        var response = controller.getTeamSagaStatus(sagaId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(saga, response.getBody());
    }

    @Test
    void preservesSynchronousTeamCreationEndpoint() {
        Team team = mock(Team.class);
        ProTeamRequest request = proTeamRequest("Example", "City", "State", List.of(), "Division");
        when(teamService.create(request)).thenReturn(team);

        var response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(team, response.getBody());
    }

    private ProTeamRequest proTeamRequest(
            String name, String city, String state, List<String> colors, String division) {
        ProTeamRequest request = new ProTeamRequest();
        request.setName(name);
        request.setCity(city);
        request.setState(state);
        request.setColors(colors);
        request.setDivision(division);
        return request;
    }
}
