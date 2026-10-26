package app.controller;

import app.dto.userdto.FavoritePlayersRequest;
import app.models.abstractEntities.Player;
import app.service.FavoriteSagaService;
import app.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteControllerTest {

    private FavoriteSagaService favoriteSagaService;
    private FavoriteService favoriteService;
    private FavoriteController controller;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        favoriteSagaService = mock(FavoriteSagaService.class);
        favoriteService = mock(FavoriteService.class);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("alice");
        controller = new FavoriteController(favoriteSagaService, favoriteService);
    }

    @Test
    void addsFavoritesForAuthenticatedUser() {
        List<Player> favorites = List.of(mock(Player.class));
        when(favoriteSagaService.addFavorites("alice", List.of(10L))).thenReturn(favorites);

        var response = controller.addFavorites(authentication, new FavoritePlayersRequest(List.of(10L)));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(favorites, response.getBody());
        verify(favoriteSagaService).addFavorites("alice", List.of(10L));
    }

    @Test
    void listsFavoritesForAuthenticatedUser() {
        List<Player> favorites = List.of(mock(Player.class));
        when(favoriteService.getFavorites("alice")).thenReturn(favorites);

        var response = controller.getFavorites(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(favorites, response.getBody());
        verify(favoriteService).getFavorites("alice");
    }

    @Test
    void removesFavoriteForAuthenticatedUser() {
        var response = controller.removeFavorite(authentication, 10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(favoriteService).removeFavoriteForUser("alice", 10L);
    }
}
