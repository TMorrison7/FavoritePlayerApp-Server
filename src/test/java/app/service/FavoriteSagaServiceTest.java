package app.service;

import app.models.abstractEntities.Player;
import app.repositories.PlayerRepository;
import app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FavoriteSagaServiceTest {

    @Test
    void compensatesCompletedFavoriteStepsInReverseOrderWhenALaterStepFails() {
        FavoriteService favoriteService = mock(FavoriteService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        Player firstPlayer = mock(Player.class);
        Player secondPlayer = mock(Player.class);
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(firstPlayer.getId()).thenReturn(10L);
        when(secondPlayer.getId()).thenReturn(20L);
        when(playerRepository.findAllById(List.of(10L, 20L)))
                .thenReturn(List.of(firstPlayer, secondPlayer));
        when(favoriteService.addFavorite("alice", 10L)).thenReturn(true);
        IllegalStateException failure = new IllegalStateException("second step failed");
        when(favoriteService.addFavorite("alice", 20L)).thenThrow(failure);

        FavoriteSagaService sagaService = new FavoriteSagaService(
                favoriteService, userRepository, playerRepository);

        assertSame(failure, assertThrows(
                IllegalStateException.class,
                () -> sagaService.addFavorites("alice", List.of(10L, 20L))));

        InOrder calls = inOrder(favoriteService);
        calls.verify(favoriteService).addFavorite("alice", 10L);
        calls.verify(favoriteService).addFavorite("alice", 20L);
        calls.verify(favoriteService).removeFavorite("alice", 10L);
        verify(favoriteService, never()).removeFavorite("alice", 20L);
    }
}
