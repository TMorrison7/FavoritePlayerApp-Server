package app.controller;



import app.dto.playerdto.request.PlayerCreateRequest;
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

    @PostMapping("/create")
    public ResponseEntity<Player> create(@Valid @RequestBody PlayerCreateRequest request){
        Player player = playerService.create(request);
        return new ResponseEntity<>(player, HttpStatus.CREATED);
    }
    @PostMapping("/search")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<List<Player>> search(@RequestBody PlayerCreateRequest request){
        List<Player> players = this.playerService.search(request);
        return ResponseEntity.ok(players);
    }
    @GetMapping
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<List<Player>> getAll(){
        List<Player> players = this.playerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }
    @GetMapping("/{id}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<Player> getById(@PathVariable Long id){
        Player player = playerService.getPlayerById(id);
        return ResponseEntity.ok(player);
    }

}
