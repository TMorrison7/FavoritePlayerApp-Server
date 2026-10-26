package app.controller;

import app.dto.userdto.FavoritePlayersRequest;
import app.dto.userdto.FavoriteSagaResponse;
import app.models.FavoriteSagaStatus;
import app.models.abstractEntities.Player;
import app.service.FavoriteSagaService;
import app.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteControllerTest {

    private FavoriteSagaService favoriteSagaService;
    private FavoriteService favoriteService;
    private FavoriteController controller;
    private Authentication authentication;
    private HttpServletRequest servletRequest;

    @BeforeEach
    void setUp() {
        favoriteSagaService = mock(FavoriteSagaService.class);
        favoriteService = mock(FavoriteService.class);
        authentication = mock(Authentication.class);
        servletRequest = mock(HttpServletRequest.class);
        when(authentication.getName()).thenReturn("alice");
        when(servletRequest.getContextPath()).thenReturn("/favorite-player");
        controller = new FavoriteController(favoriteSagaService, favoriteService);
    }

    @Test
    void addsFavoritesForAuthenticatedUser() {
        FavoriteSagaResponse saga = new FavoriteSagaResponse(
                UUID.randomUUID(), FavoriteSagaStatus.PENDING, null);
        when(favoriteSagaService.start("alice", List.of(10L))).thenReturn(saga);

        var response = controller.addFavorites(
                authentication, servletRequest, new FavoritePlayersRequest(List.of(10L)));

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(saga, response.getBody());
        assertEquals("/favorite-player/api/user/favorites/sagas/" + saga.sagaId(),
                response.getHeaders().getFirst("Location"));
        verify(favoriteSagaService).start("alice", List.of(10L));
    }

    @Test
    void getsSagaStatusForAuthenticatedUser() {
        UUID sagaId = UUID.randomUUID();
        FavoriteSagaResponse saga = new FavoriteSagaResponse(sagaId, FavoriteSagaStatus.COMPLETED, null);
        when(favoriteSagaService.getStatus("alice", sagaId)).thenReturn(saga);

        var response = controller.getSagaStatus(authentication, sagaId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(saga, response.getBody());
        verify(favoriteSagaService).getStatus("alice", sagaId);
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
