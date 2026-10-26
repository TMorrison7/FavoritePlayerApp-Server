package app.controller;


import app.dto.playerdto.PlayerRequest;
import app.models.abstractEntities.Player;
import app.service.PlayerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/players")
@AllArgsConstructor
public class PlayerController {

    private PlayerService playerService;

    @PostMapping("/create")
    public ResponseEntity<Player> create(@Valid @RequestBody PlayerRequest request){
        Player player = playerService.create(request);
        return new ResponseEntity<>(player, HttpStatus.CREATED);
    }
    @PostMapping("/search")
    public ResponseEntity<List<Player>> search(@RequestBody PlayerRequest request){
        List<Player> players = playerService.search(request);
        return ResponseEntity.ok(players);
    }
    @GetMapping
    public ResponseEntity<List<Player>> getAll(){
        List<Player> players = playerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Player> getById(@PathVariable Long id){
        Player player = playerService.getPlayerById(id);
        return ResponseEntity.ok(player);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Player> updateFull(@PathVariable Long id, @RequestBody PlayerRequest request){
        Player player = playerService.updateFull(id, request);
        return ResponseEntity.ok(player);
    }
    @PatchMapping("/{id}")
    public ResponseEntity<Player> updatePartial(@PathVariable Long id, @RequestBody PlayerRequest request){
        Player player = playerService.updatePartial(id, request);
        return ResponseEntity.ok(player);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
