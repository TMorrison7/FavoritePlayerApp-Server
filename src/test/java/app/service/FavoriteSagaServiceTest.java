package app.service;

import app.exception.ResourceNotFoundException;
import app.models.abstractEntities.Player;
import app.repositories.PlayerRepository;
import app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteSagaServiceTest {

    private FavoriteService favoriteService;
    private UserRepository userRepository;
    private PlayerRepository playerRepository;
    private FavoriteSagaService sagaService;

    @BeforeEach
    void setUp() {
        favoriteService = mock(FavoriteService.class);
        userRepository = mock(UserRepository.class);
        playerRepository = mock(PlayerRepository.class);
        sagaService = new FavoriteSagaService(favoriteService, userRepository, playerRepository);
    }

    @Test
    void addsUniquePlayersAndReturnsFavorites() {
        Player first = player(10L);
        Player second = player(20L);
        List<Player> favorites = List.of(first, second);
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(playerRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(first, second));
        when(favoriteService.addFavorite("alice", 10L)).thenReturn(true);
        when(favoriteService.addFavorite("alice", 20L)).thenReturn(true);
        when(favoriteService.getFavorites("alice")).thenReturn(favorites);

        assertSame(favorites, sagaService.addFavorites("alice", List.of(10L, 10L, 20L)));

        verify(playerRepository).findAllById(List.of(10L, 20L));
        verify(favoriteService).addFavorite("alice", 10L);
        verify(favoriteService).addFavorite("alice", 20L);
        verify(favoriteService).getFavorites("alice");
        verify(favoriteService, never()).removeFavorite(anyString(), anyLong());
    }

    @Test
    void doesNotClaimOrCompensateFavoritesThatAlreadyExisted() {
        Player first = player(10L);
        Player second = player(20L);
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(playerRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(first, second));
        when(favoriteService.addFavorite("alice", 10L)).thenReturn(false);
        when(favoriteService.addFavorite("alice", 20L)).thenThrow(new IllegalStateException("failed"));

        assertThrows(IllegalStateException.class,
                () -> sagaService.addFavorites("alice", List.of(10L, 20L)));

        verify(favoriteService, never()).removeFavorite("alice", 10L);
    }

    @Test
    void rejectsUnknownUserBeforeLookingUpPlayers() {
        when(userRepository.existsByUsername("missing")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> sagaService.addFavorites("missing", List.of(10L)));

        verifyNoInteractions(playerRepository, favoriteService);
    }

    @Test
    void rejectsUnknownPlayerBeforeStartingSaga() {
        Player knownPlayer = player(10L);
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(playerRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(knownPlayer));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> sagaService.addFavorites("alice", List.of(10L, 20L)));

        assertEquals("Player with ID: 20 not found.", exception.getMessage());
        verifyNoInteractions(favoriteService);
    }

    @Test
    void compensatesCompletedStepsInReverseOrderWhenALaterStepFails() {
        preparePlayers(10L, 20L, 30L);
        when(favoriteService.addFavorite("alice", 10L)).thenReturn(true);
        when(favoriteService.addFavorite("alice", 20L)).thenReturn(true);
        IllegalStateException failure = new IllegalStateException("third step failed");
        when(favoriteService.addFavorite("alice", 30L)).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> sagaService.addFavorites("alice", List.of(10L, 20L, 30L))));

        InOrder calls = inOrder(favoriteService);
        calls.verify(favoriteService).addFavorite("alice", 10L);
        calls.verify(favoriteService).addFavorite("alice", 20L);
        calls.verify(favoriteService).addFavorite("alice", 30L);
        calls.verify(favoriteService).removeFavorite("alice", 20L);
        calls.verify(favoriteService).removeFavorite("alice", 10L);
        verify(favoriteService, never()).removeFavorite("alice", 30L);
    }

    @Test
    void preservesOriginalFailureAndContinuesCompensatingWhenCompensationFails() {
        preparePlayers(10L, 20L, 30L);
        when(favoriteService.addFavorite("alice", 10L)).thenReturn(true);
        when(favoriteService.addFavorite("alice", 20L)).thenReturn(true);
        IllegalStateException failure = new IllegalStateException("third step failed");
        when(favoriteService.addFavorite("alice", 30L)).thenThrow(failure);
        IllegalStateException compensationFailure = new IllegalStateException("rollback failed");
        doThrow(compensationFailure).when(favoriteService).removeFavorite("alice", 20L);

        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> sagaService.addFavorites("alice", List.of(10L, 20L, 30L))));

        assertArrayEquals(new Throwable[]{compensationFailure}, failure.getSuppressed());
        verify(favoriteService).removeFavorite("alice", 20L);
        verify(favoriteService).removeFavorite("alice", 10L);
    }

    @Test
    void compensatesAddedFavoritesWhenReadingFinalFavoriteListFails() {
        preparePlayers(10L, 20L);
        when(favoriteService.addFavorite("alice", 10L)).thenReturn(true);
        when(favoriteService.addFavorite("alice", 20L)).thenReturn(true);
        IllegalStateException failure = new IllegalStateException("favorite read failed");
        when(favoriteService.getFavorites("alice")).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> sagaService.addFavorites("alice", List.of(10L, 20L))));

        InOrder calls = inOrder(favoriteService);
        calls.verify(favoriteService).removeFavorite("alice", 20L);
        calls.verify(favoriteService).removeFavorite("alice", 10L);
    }

    @Test
    void returnsCurrentFavoritesForAnEmptyBatch() {
        List<Player> favorites = List.of(player(50L));
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(playerRepository.findAllById(List.of())).thenReturn(List.of());
        when(favoriteService.getFavorites("alice")).thenReturn(favorites);

        assertSame(favorites, sagaService.addFavorites("alice", List.of()));

        verify(favoriteService, never()).addFavorite(anyString(), anyLong());
        verify(favoriteService, never()).removeFavorite(anyString(), anyLong());
    }

    private void preparePlayers(Long... ids) {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        List<Long> playerIds = List.of(ids);
        when(playerRepository.findAllById(playerIds)).thenReturn(playerIds.stream().map(this::player).toList());
    }

    private Player player(Long id) {
        Player player = mock(Player.class);
        when(player.getId()).thenReturn(id);
        return player;
    }
}
