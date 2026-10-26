package app.service;

import app.config.PlayerCacheConfiguration;
import app.dto.playerdto.BasketballRequest;
import app.models.abstractEntities.Player;
import app.repositories.PlayerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(PlayerServiceCacheTest.TestConfiguration.class)
class PlayerServiceCacheTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearState() {
        reset(playerRepository);
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    void cachesAllPlayers() {
        List<Player> players = List.of(mock(Player.class));
        when(playerRepository.findAll()).thenReturn(players);

        assertSame(players, playerService.getAllPlayers());
        assertSame(players, playerService.getAllPlayers());

        verify(playerRepository, times(1)).findAll();
    }

    @Test
    void cachesPlayerById() {
        Player player = mock(Player.class);
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));

        assertSame(player, playerService.getPlayerById(10L));
        assertSame(player, playerService.getPlayerById(10L));

        verify(playerRepository, times(1)).findById(10L);
    }

    @Test
    void cachesSearchUsingFilterValuesRatherThanRequestObjectIdentity() {
        List<Player> players = List.of(mock(Player.class));
        when(playerRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(players);
        BasketballRequest firstRequest = searchRequest("Jordan");
        BasketballRequest equivalentRequest = searchRequest("Jordan");

        assertSame(players, playerService.search(firstRequest));
        assertSame(players, playerService.search(equivalentRequest));

        verify(playerRepository, times(1))
                .findAll(any(org.springframework.data.jpa.domain.Specification.class));
    }

    @Test
    void evictsPlayerCachesAfterSuccessfulDelete() {
        List<Player> cachedPlayers = List.of(mock(Player.class));
        List<Player> updatedPlayers = List.of(mock(Player.class));
        when(playerRepository.findAll()).thenReturn(cachedPlayers, updatedPlayers);
        when(playerRepository.existsById(10L)).thenReturn(true);

        assertSame(cachedPlayers, playerService.getAllPlayers());
        playerService.delete(10L);

        assertSame(updatedPlayers, playerService.getAllPlayers());
        verify(playerRepository, times(2)).findAll();
        verify(playerRepository).deleteById(10L);
    }

    @Test
    void doesNotEvictPlayerCachesWhenDeleteFails() {
        List<Player> players = List.of(mock(Player.class));
        when(playerRepository.findAll()).thenReturn(players);
        when(playerRepository.existsById(10L)).thenReturn(false);

        assertSame(players, playerService.getAllPlayers());
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> playerService.delete(10L));
        assertSame(players, playerService.getAllPlayers());

        verify(playerRepository, times(1)).findAll();
    }

    private BasketballRequest searchRequest(String firstName) {
        BasketballRequest request = new BasketballRequest();
        request.setFirstName(firstName);
        return request;
    }

    @Configuration
    @Import({PlayerCacheConfiguration.class, PlayerService.class})
    static class TestConfiguration {

        @Bean
        PlayerRepository playerRepository() {
            return mock(PlayerRepository.class);
        }

        @Bean
        Validator validator() {
            return mock(Validator.class);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
