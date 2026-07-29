package app.controller;


import app.models.abstractEntities.Player;
import app.service.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/players")
@AllArgsConstructor
public class PlayerController {

    private PlayerService playerService;


    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers(){
        List<Player> players = this.playerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }
}
