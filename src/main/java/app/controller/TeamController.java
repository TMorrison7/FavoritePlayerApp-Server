package app.controller;


import app.dto.teamdto.TeamRequest;
import app.dto.teamdto.TeamCreationSagaRequest;
import app.dto.teamdto.TeamSagaResponse;
import app.models.abstractEntities.Team;
import app.service.TeamService;
import app.service.TeamSagaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/create")
    public ResponseEntity<Team> create(@Valid @RequestBody TeamRequest teamRequest){
        Team team = teamService.create(teamRequest);
        return new ResponseEntity<>(team, HttpStatus.CREATED);
    }

    @PostMapping("/search")
    public ResponseEntity<List<Team>> search(@RequestBody TeamRequest request){
        List<Team> teams = teamService.search(request);
        return ResponseEntity.ok(teams);
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
    public ResponseEntity<List<Team>> getAll(){
        List<Team> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id){
        Team team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> updateFull(@PathVariable Long id, @RequestBody TeamRequest request){
        Team team = teamService.updateFull(id, request);
        return ResponseEntity.ok(team);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Team> updatePartial(@PathVariable Long id, @RequestBody TeamRequest request){
        Team team = teamService.updatePartial(id, request);
        return ResponseEntity.ok(team);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }




}
