package app.service;

import app.models.Favorite;
import app.models.FavoriteSaga;
import app.models.FavoriteSagaStatus;
import app.models.User;
import app.models.abstractEntities.Player;
import app.repositories.FavoriteRepository;
import app.repositories.FavoriteSagaRepository;
import app.repositories.PlayerRepository;
import app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FavoriteSagaStepServiceTest {

    private FavoriteSagaRepository sagaRepository;
    private FavoriteRepository favoriteRepository;
    private UserRepository userRepository;
    private PlayerRepository playerRepository;
    private FavoriteSagaStepService stepService;
    private FavoriteSaga saga;

    @BeforeEach
    void setUp() {
        sagaRepository = mock(FavoriteSagaRepository.class);
        favoriteRepository = mock(FavoriteRepository.class);
        userRepository = mock(UserRepository.class);
        playerRepository = mock(PlayerRepository.class);
        stepService = new FavoriteSagaStepService(
                sagaRepository, favoriteRepository, userRepository, playerRepository);
        saga = new FavoriteSaga("alice", List.of(10L, 20L));
        saga.setStatus(FavoriteSagaStatus.PROCESSING);
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
    }

    @Test
    void persistsNewFavoriteAndProgressInSameStep() {
        User user = new User();
        Player player = mock(Player.class);
        when(favoriteRepository.findByUser_UsernameAndPlayer_Id("alice", 10L)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));

        stepService.addFavorite(saga.getId(), 10L);

        verify(favoriteRepository).saveAndFlush(argThat(favorite ->
                favorite.getUser() == user && favorite.getPlayer() == player));
        assertEquals(List.of(10L), saga.getCompletedPlayerIds());
    }

    @Test
    void leavesPreviouslyExistingFavoriteOutOfCompensationState() {
        Favorite existingFavorite = new Favorite(new User(), mock(Player.class));
        when(favoriteRepository.findByUser_UsernameAndPlayer_Id("alice", 10L))
                .thenReturn(Optional.of(existingFavorite));

        stepService.addFavorite(saga.getId(), 10L);

        assertTrue(saga.getCompletedPlayerIds().isEmpty());
        verify(favoriteRepository, never()).saveAndFlush(any(Favorite.class));
        verifyNoInteractions(userRepository, playerRepository);
    }

    @Test
    void makesRepeatedCompletedStepIdempotent() {
        saga.getCompletedPlayerIds().add(10L);

        stepService.addFavorite(saga.getId(), 10L);

        verifyNoInteractions(favoriteRepository, userRepository, playerRepository);
    }

    @Test
    void compensatesMostRecentlyCompletedFavoriteFirst() {
        Favorite favorite = new Favorite(new User(), mock(Player.class));
        saga.setStatus(FavoriteSagaStatus.COMPENSATING);
        saga.getCompletedPlayerIds().addAll(List.of(10L, 20L));
        when(favoriteRepository.findByUser_UsernameAndPlayer_Id("alice", 20L))
                .thenReturn(Optional.of(favorite));

        assertEquals(20L, stepService.compensateNext(saga.getId()));

        assertEquals(List.of(10L), saga.getCompletedPlayerIds());
        verify(favoriteRepository).delete(favorite);
    }

    @Test
    void beginMovesPendingSagaToProcessing() {
        saga.setStatus(FavoriteSagaStatus.PENDING);

        FavoriteSagaExecution started = stepService.begin(saga.getId());

        assertEquals(FavoriteSagaStatus.PROCESSING, started.status());
        assertEquals(List.of(10L, 20L), started.playerIds());
    }
}
