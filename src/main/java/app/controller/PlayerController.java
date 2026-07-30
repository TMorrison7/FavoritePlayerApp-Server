package app.controller;


import app.dto.PlayerRequestDto;
import app.models.abstractEntities.Player;
import app.service.PlayerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/players")
@AllArgsConstructor
public class PlayerController {

    private PlayerService playerService;

    @PostMapping
    public ResponseEntity<Player> createPlayer(@Valid @RequestBody PlayerRequestDto playerDto){
        Player player = playerService.createPlayer(playerDto);
        return new ResponseEntity<>(player, HttpStatus.CREATED);
    }
    @GetMapping
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<List<Player>> getAllPlayers(){
        List<Player> players = this.playerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/{id}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id){
        Player player = this.playerService.getPlayerById(id);
        return ResponseEntity.ok(player);
    }

    @GetMapping("/search/name")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<Player> getPlayerByFirstAndLastName(@RequestParam String firstName, @RequestParam String lastname){
        Player player = this.playerService.getPlayerByFirstAndLastName(firstName, lastname);
        return ResponseEntity.ok(player);
    }
    @GetMapping("/search/firstname")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<List<Player>> getPlayersByFirstName(@RequestParam String firstName){
        List<Player> players = this.playerService.getPlayersByFirstName(firstName);
        return ResponseEntity.ok(players);
    }
    @GetMapping("/search/lastname")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<List<Player>>getPlayersByLastName(@RequestParam String lastname){
        List<Player> players = this.playerService.getPlayersByLastName(lastname);
        return ResponseEntity.ok(players);
    }
    @GetMapping("/search/state")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<List<Player>> getPlayersByState(@RequestParam String state){
        List<Player> players = this.playerService.getPlayersByState(state);
        return ResponseEntity.ok(players);
    }
    @GetMapping("/search/city")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<List<Player>> getPlayersByCity(@RequestParam String city){
        List<Player> players = this.playerService.getPlayersByCity(city);
        return ResponseEntity.ok(players);
    }
    @GetMapping("/search/high-school")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<List<Player>> getPlayersByHighSchool(@RequestParam String highSchool){
        List<Player> players = this.playerService.getPlayersByHighSchool(highSchool);
        return ResponseEntity.ok(players);
    }



}
