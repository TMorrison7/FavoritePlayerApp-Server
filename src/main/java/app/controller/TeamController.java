package app.controller;


import app.dto.teamdto.TeamRequestDto;
import app.dto.teamdto.TeamCreationSagaRequest;
import app.dto.teamdto.TeamSagaResponse;
import app.models.abstractEntities.Team;
import app.service.TeamService;
import app.service.TeamSagaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teams")
@AllArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TeamSagaService teamSagaService;

    @PostMapping
    public ResponseEntity<Team> createTeam(@Valid @RequestBody TeamRequestDto teamRequestDto){
        Team team = teamService.createTeam(teamRequestDto);
        return new ResponseEntity<>(team, HttpStatus.CREATED);
    }

    @PostMapping("/sagas")
    public ResponseEntity<TeamSagaResponse> createTeamSaga(
            @Valid @RequestBody TeamCreationSagaRequest request,
            HttpServletRequest servletRequest) {
        TeamSagaResponse saga = teamSagaService.start(request);
        return ResponseEntity.accepted()
                .header("Location", servletRequest.getContextPath() + "/teams/sagas/" + saga.sagaId())
                .body(saga);
    }

    @GetMapping("/sagas/{sagaId}")
    public ResponseEntity<TeamSagaResponse> getTeamSagaStatus(@PathVariable UUID sagaId) {
        return ResponseEntity.ok(teamSagaService.getStatus(sagaId));
    }

    @GetMapping
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<List<Team>> getAllTeams(){
        List<Team> teams = this.teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<Team> getTeamById(@PathVariable Long id){
        Team team = this.teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @GetMapping("/search/name")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<Team> getTeamByName(@RequestParam String name){
        Team team = this.teamService.getTeamByName(name);
        return ResponseEntity.ok(team);
    }


}
