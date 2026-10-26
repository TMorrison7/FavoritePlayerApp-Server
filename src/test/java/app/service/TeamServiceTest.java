package app.service;

import app.dto.teamdto.CollegeTeamRequest;
import app.dto.teamdto.ProTeamRequest;
import app.dto.teamdto.TeamRequest;
import app.models.entities.College;
import app.models.entities.ProTeam;
import app.repositories.TeamRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeamServiceTest {

    private TeamRepository teamRepository;
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamRepository = mock(TeamRepository.class);
        Validator validator = mock(Validator.class);
        when(validator.validate(any(TeamRequest.class))).thenReturn(Set.of());
        teamService = new TeamService(teamRepository, validator);
        when(teamRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsProfessionalTeamSubtype() {
        var team = teamService.create(proTeamRequest());

        assertInstanceOf(ProTeam.class, team);
    }

    @Test
    void createsCollegeTeamSubtype() {
        var team = teamService.create(collegeTeamRequest());

        assertInstanceOf(College.class, team);
    }

    private ProTeamRequest proTeamRequest() {
        ProTeamRequest request = new ProTeamRequest();
        request.setName("Example");
        request.setCity("City");
        request.setState("State");
        request.setColors(List.of("blue"));
        request.setDivision("Division");
        return request;
    }

    private CollegeTeamRequest collegeTeamRequest() {
        CollegeTeamRequest request = new CollegeTeamRequest();
        request.setName("Example");
        request.setCity("City");
        request.setState("State");
        request.setColors(List.of("blue"));
        request.setDivision("Division");
        return request;
    }
}
