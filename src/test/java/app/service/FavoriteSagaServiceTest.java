package app.service;

import app.dto.userdto.FavoriteSagaResponse;
import app.dto.userdto.FavoriteSagaStartedEvent;
import app.exception.ResourceNotFoundException;
import app.models.FavoriteSaga;
import app.models.FavoriteSagaStatus;
import app.models.OutboxEvent;
import app.models.abstractEntities.Player;
import app.repositories.FavoriteSagaRepository;
import app.repositories.OutboxEventRepository;
import app.repositories.PlayerRepository;
import app.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FavoriteSagaServiceTest {

    private FavoriteSagaRepository sagaRepository;
    private OutboxEventRepository outboxRepository;
    private UserRepository userRepository;
    private PlayerRepository playerRepository;
    private ObjectMapper objectMapper;
    private FavoriteSagaService sagaService;

    @BeforeEach
    void setUp() {
        sagaRepository = mock(FavoriteSagaRepository.class);
        outboxRepository = mock(OutboxEventRepository.class);
        userRepository = mock(UserRepository.class);
        playerRepository = mock(PlayerRepository.class);
        objectMapper = new ObjectMapper();
        sagaService = new FavoriteSagaService(
                sagaRepository, outboxRepository, userRepository, playerRepository, objectMapper);
        ReflectionTestUtils.setField(sagaService, "favoriteSagaTopic", "favorite-saga-events");
    }

    @Test
    void createsSagaAndOutboxEventForValidatedUniquePlayerIds() throws Exception {
        Player first = player(10L);
        Player second = player(20L);
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(playerRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(first, second));
        when(sagaRepository.save(any(FavoriteSaga.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteSagaResponse response = sagaService.start("alice", List.of(10L, 10L, 20L));

        assertEquals(FavoriteSagaStatus.PENDING, response.status());
        assertNull(response.failureMessage());
        var eventCaptor = org.mockito.ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(eventCaptor.capture());
        OutboxEvent event = eventCaptor.getValue();
        assertEquals("favorite-saga-events", event.getTopic());
        assertEquals(response.sagaId().toString(), event.getMessageKey());
        assertEquals(new FavoriteSagaStartedEvent(response.sagaId()),
                objectMapper.readValue(event.getPayload(), FavoriteSagaStartedEvent.class));
        verify(sagaRepository).save(argThat(saga ->
                saga.getPlayerIds().equals(List.of(10L, 20L))
                        && saga.getUsername().equals("alice")));
    }

    @Test
    void rejectsUnknownUserWithoutCreatingSagaOrOutboxEvent() {
        when(userRepository.existsByUsername("missing")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> sagaService.start("missing", List.of(10L)));

        verifyNoInteractions(playerRepository, sagaRepository, outboxRepository);
    }

    @Test
    void rejectsUnknownPlayerBeforePersistingSagaOrOutboxEvent() {
        Player knownPlayer = player(10L);
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(playerRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(knownPlayer));

        assertThrows(ResourceNotFoundException.class,
                () -> sagaService.start("alice", List.of(10L, 20L)));

        verifyNoInteractions(sagaRepository, outboxRepository);
    }

    @Test
    void returnsSagaStatusOnlyToItsOwningUser() {
        UUID id = UUID.randomUUID();
        FavoriteSaga saga = new FavoriteSaga("alice", List.of(10L));
        saga.setId(id.toString());
        saga.setStatus(FavoriteSagaStatus.COMPENSATING);
        saga.setFailureMessage("Player step failed");
        when(sagaRepository.findByIdAndUsername(id.toString(), "alice")).thenReturn(Optional.of(saga));

        FavoriteSagaResponse response = sagaService.getStatus("alice", id);

        assertEquals(id, response.sagaId());
        assertEquals(FavoriteSagaStatus.COMPENSATING, response.status());
        assertEquals("Player step failed", response.failureMessage());
    }

    @Test
    void doesNotExposeSagaStatusToAnotherUser() {
        UUID id = UUID.randomUUID();
        when(sagaRepository.findByIdAndUsername(id.toString(), "mallory")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sagaService.getStatus("mallory", id));
    }

    private Player player(Long id) {
        Player player = mock(Player.class);
        when(player.getId()).thenReturn(id);
        return player;
    }
}
