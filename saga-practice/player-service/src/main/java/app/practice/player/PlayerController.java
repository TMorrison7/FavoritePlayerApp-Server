package app.practice.player;

import app.practice.contracts.AssignPlayerCommand;
import app.practice.contracts.CreatePlayerRequest;
import app.practice.contracts.PlayerResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class PlayerController {

    private final PlayerApplicationService playerService;

    public PlayerController(PlayerApplicationService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/players")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse create(@Valid @RequestBody CreatePlayerRequest request) {
        return playerService.create(request);
    }

    @GetMapping("/players")
    public List<PlayerResponse> findAll() {
        return playerService.findAll();
    }

    @PostMapping("/internal/player-assignments")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assign(@Valid @RequestBody AssignPlayerCommand command) {
        playerService.assign(command);
    }

    @PostMapping("/internal/player-assignments/{sagaId}/compensate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void compensate(@PathVariable String sagaId) {
        playerService.compensate(sagaId);
    }
}
