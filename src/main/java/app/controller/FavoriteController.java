package app.controller;

import app.dto.userdto.FavoritePlayersRequest;
import app.dto.userdto.FavoriteSagaResponse;
import app.models.abstractEntities.Player;
import app.service.FavoriteSagaService;
import app.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/favorites")
@AllArgsConstructor
public class FavoriteController {

    private final FavoriteSagaService favoriteSagaService;
    private final FavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<FavoriteSagaResponse> addFavorites(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @Valid @RequestBody FavoritePlayersRequest request) {
        FavoriteSagaResponse saga = favoriteSagaService.start(authentication.getName(), request.playerIds());
        return ResponseEntity.accepted()
                .header("Location", servletRequest.getContextPath()
                        + "/api/user/favorites/sagas/" + saga.sagaId())
                .body(saga);
    }

    @GetMapping("/sagas/{sagaId}")
    public ResponseEntity<FavoriteSagaResponse> getSagaStatus(
            Authentication authentication,
            @PathVariable UUID sagaId) {
        return ResponseEntity.ok(favoriteSagaService.getStatus(authentication.getName(), sagaId));
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
