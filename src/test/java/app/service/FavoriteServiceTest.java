package app.service;

import app.exception.ResourceNotFoundException;
import app.models.Favorite;
import app.models.User;
import app.models.abstractEntities.Player;
import app.repositories.FavoriteRepository;
import app.repositories.PlayerRepository;
import app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteServiceTest {

    private FavoriteRepository favoriteRepository;
    private UserRepository userRepository;
    private PlayerRepository playerRepository;
    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() {
        favoriteRepository = mock(FavoriteRepository.class);
        userRepository = mock(UserRepository.class);
        playerRepository = mock(PlayerRepository.class);
        favoriteService = new FavoriteService(favoriteRepository, userRepository, playerRepository);
    }

    @Test
    void addsFavoriteAfterResolvingUserAndPlayer() {
        User user = new User();
        Player player = mock(Player.class);
        Favorite savedFavorite = new Favorite(user, player);
        when(favoriteRepository.findByUser_UsernameAndPlayer_Id("alice", 10L)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(favoriteRepository.saveAndFlush(any(Favorite.class))).thenReturn(savedFavorite);

        assertTrue(favoriteService.addFavorite("alice", 10L));

        verify(favoriteRepository).saveAndFlush(argThat(favorite ->
                favorite.getUser() == user && favorite.getPlayer() == player));
    }

    @Test
    void returnsFalseWithoutWritesWhenFavoriteAlreadyExists() {
        when(favoriteRepository.findByUser_UsernameAndPlayer_Id("alice", 10L))
                .thenReturn(Optional.of(new Favorite(new User(), mock(Player.class))));

        assertFalse(favoriteService.addFavorite("alice", 10L));

        verifyNoInteractions(userRepository, playerRepository);
        verify(favoriteRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsFavoriteForUnknownUser() {
        when(favoriteRepository.findByUser_UsernameAndPlayer_Id("alice", 10L)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> favoriteService.addFavorite("alice", 10L));

        verifyNoInteractions(playerRepository);
        verify(favoriteRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsFavoriteForUnknownPlayer() {
        when(favoriteRepository.findByUser_UsernameAndPlayer_Id("alice", 10L)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new User()));
        when(playerRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> favoriteService.addFavorite("alice", 10L));

        verify(favoriteRepository, never()).saveAndFlush(any());
    }

    @Test
    void removesSagaStepWithoutPreconditionLookup() {
        favoriteService.removeFavorite("alice", 10L);

        verify(favoriteRepository).deleteByUser_UsernameAndPlayer_Id("alice", 10L);
        verifyNoInteractions(userRepository, playerRepository);
    }

    @Test
    void returnsFavoritesForExistingUser() {
        Player player = mock(Player.class);
        List<Favorite> favorites = List.of(new Favorite(new User(), player));
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(favoriteRepository.findAllByUser_UsernameOrderById("alice")).thenReturn(favorites);

        assertEquals(List.of(player), favoriteService.getFavorites("alice"));
    }

    @Test
    void rejectsListingFavoritesForUnknownUser() {
        when(userRepository.existsByUsername("missing")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> favoriteService.getFavorites("missing"));

        verify(favoriteRepository, never()).findAllByUser_UsernameOrderById(anyString());
    }

    @Test
    void removesExistingFavoriteForUser() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(favoriteRepository.findByUser_UsernameAndPlayer_Id("alice", 10L))
                .thenReturn(Optional.of(new Favorite(new User(), mock(Player.class))));

        favoriteService.removeFavoriteForUser("alice", 10L);

        verify(favoriteRepository).deleteByUser_UsernameAndPlayer_Id("alice", 10L);
    }

    @Test
    void rejectsRemovalForUnknownUser() {
        when(userRepository.existsByUsername("missing")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> favoriteService.removeFavoriteForUser("missing", 10L));

        verifyNoInteractions(favoriteRepository);
    }

    @Test
    void rejectsRemovalWhenPlayerIsNotFavorite() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(favoriteRepository.findByUser_UsernameAndPlayer_Id("alice", 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> favoriteService.removeFavoriteForUser("alice", 10L));

        verify(favoriteRepository, never()).deleteByUser_UsernameAndPlayer_Id(anyString(), anyLong());
    }
}
