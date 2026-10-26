package app.controller;

import app.dto.userdto.FavoritePlayersRequest;
import app.models.abstractEntities.Player;
import app.service.FavoriteSagaService;
import app.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/favorites")
@AllArgsConstructor
public class FavoriteController {

    private final FavoriteSagaService favoriteSagaService;
    private final FavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<List<Player>> addFavorites(
            Authentication authentication,
            @Valid @RequestBody FavoritePlayersRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoriteSagaService.addFavorites(authentication.getName(), request.playerIds()));
    }

    @GetMapping
    public ResponseEntity<List<Player>> getFavorites(Authentication authentication) {
        return ResponseEntity.ok(favoriteService.getFavorites(authentication.getName()));
    }

    @DeleteMapping("/{playerId}")
    public ResponseEntity<Void> removeFavorite(
            Authentication authentication,
            @PathVariable Long playerId) {
        favoriteService.removeFavoriteForUser(authentication.getName(), playerId);
        return ResponseEntity.noContent().build();
    }
}
