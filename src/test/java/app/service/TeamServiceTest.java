package app.service;

import app.dto.teamdto.CollegeTeamRequestDto;
import app.dto.teamdto.ProTeamRequestDto;
import app.models.entities.College;
import app.models.entities.ProTeam;
import app.repositories.TeamRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        teamService = new TeamService(teamRepository, validator);
        when(teamRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsProfessionalTeamSubtype() {
        var team = teamService.createTeam(
                new ProTeamRequestDto("Example", "City", "State", List.of("blue"), "Division"));

        assertInstanceOf(ProTeam.class, team);
    }

    @Test
    void createsCollegeTeamSubtype() {
        var team = teamService.createTeam(
                new CollegeTeamRequestDto("Example", "City", "State", List.of("blue"), "Division"));

        assertInstanceOf(College.class, team);
    }
}
