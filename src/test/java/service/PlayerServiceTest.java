package service;

import app.models.User;
import app.models.abstractEntities.Player;
import app.models.entities.FootballPlayer;
import app.repositories.PlayerRepository;
import app.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {
    @Mock private PlayerRepository playerRepository;
    @InjectMocks private PlayerService playerService;

    @Test
    void test() {
        Player mockPlayer = FootballPlayer.builder()
                .id(1L)
                .firstName("Hello")
                .lastName("World")
                .build();

        Mockito.when(playerRepository.findById(1L)).thenReturn(Optional.of(mockPlayer));

        Player result = playerService.getPlayerById(1L);
        assertEquals("Hello", result.getFirstName());
    }
}
