package app.practice.team;

import app.practice.contracts.CreateTeamCommand;
import app.practice.contracts.TeamCreatedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/teams")
public class TeamController {

    private final TeamApplicationService teamService;

    public TeamController(TeamApplicationService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public TeamCreatedResponse create(@Valid @RequestBody CreateTeamCommand command) {
        return teamService.create(command);
    }

    @GetMapping("/by-saga/{sagaId}")
    public TeamCreatedResponse findBySaga(@PathVariable String sagaId) {
        return teamService.findBySaga(sagaId);
    }

    @DeleteMapping("/by-saga/{sagaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void compensateCreate(@PathVariable String sagaId) {
        teamService.compensateCreate(sagaId);
    }
}
