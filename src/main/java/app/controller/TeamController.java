package app.controller;


import app.dto.teamdto.TeamRequestDto;
import app.models.abstractEntities.Team;
import app.service.TeamService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@AllArgsConstructor
public class TeamController {

    private TeamService teamService;

    @PostMapping
    public ResponseEntity<Team> createTeam(@Valid @RequestBody TeamRequestDto teamRequestDto){
        Team team = teamService.createTeam(teamRequestDto);
        return new ResponseEntity<>(team, HttpStatus.CREATED);
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
