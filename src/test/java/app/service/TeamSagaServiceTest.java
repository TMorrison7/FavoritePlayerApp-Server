package app.service;

import app.dto.teamdto.ProTeamRequestDto;
import app.dto.teamdto.TeamCreationSagaRequest;
import app.dto.teamdto.TeamSagaResponse;
import app.dto.teamdto.TeamSagaStartedEvent;
import app.exception.ResourceNotFoundException;
import app.models.OutboxEvent;
import app.models.TeamSaga;
import app.models.TeamSagaStatus;
import app.models.abstractEntities.Player;
import app.repositories.OutboxEventRepository;
import app.repositories.PlayerRepository;
import app.repositories.TeamSagaRepository;
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

class TeamSagaServiceTest {

    private TeamSagaRepository sagaRepository;
    private OutboxEventRepository outboxRepository;
    private PlayerRepository playerRepository;
    private ObjectMapper objectMapper;
    private TeamSagaService service;

    @BeforeEach
    void setUp() {
        sagaRepository = mock(TeamSagaRepository.class);
        outboxRepository = mock(OutboxEventRepository.class);
        playerRepository = mock(PlayerRepository.class);
        objectMapper = new ObjectMapper();
        service = new TeamSagaService(sagaRepository, outboxRepository, playerRepository, objectMapper);
        ReflectionTestUtils.setField(service, "teamSagaTopic", "team-saga-events");
    }

    @Test
    void persistsTeamSagaAndOutboxEventWithUniquePlayerIds() throws Exception {
        TeamCreationSagaRequest request = new TeamCreationSagaRequest(
                new ProTeamRequestDto("Warriors", "San Francisco", "CA", List.of("blue"), "NBA"),
                List.of(10L, 10L, 20L));
        Player firstPlayer = player(10L);
        Player secondPlayer = player(20L);
        when(playerRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(firstPlayer, secondPlayer));
        when(sagaRepository.save(any(TeamSaga.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamSagaResponse response = service.start(request);

        assertEquals(TeamSagaStatus.PENDING, response.status());
        assertNull(response.teamId());
        var captor = org.mockito.ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(captor.capture());
        OutboxEvent event = captor.getValue();
        assertEquals("team-saga-events", event.getTopic());
        assertEquals(response.sagaId().toString(), event.getMessageKey());
        assertEquals(new TeamSagaStartedEvent(response.sagaId()),
                objectMapper.readValue(event.getPayload(), TeamSagaStartedEvent.class));
        verify(sagaRepository).save(argThat(saga ->
                saga.getTeamType().equals("PRO")
                        && saga.getTeamName().equals("Warriors")
                        && saga.getPlayerIds().equals(List.of(10L, 20L))));
    }

    @Test
    void rejectsUnknownPlayerBeforeWritingSagaOrOutbox() {
        Player firstPlayer = player(10L);
        when(playerRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(firstPlayer));
        TeamCreationSagaRequest request = new TeamCreationSagaRequest(
                new ProTeamRequestDto("Warriors", null, null, List.of(), "NBA"),
                List.of(10L, 20L));

        assertThrows(ResourceNotFoundException.class, () -> service.start(request));

        verifyNoInteractions(sagaRepository, outboxRepository);
    }

    @Test
    void returnsPersistedStatusAndTeamId() {
        UUID id = UUID.randomUUID();
        TeamSaga saga = new TeamSaga(
                new ProTeamRequestDto("Warriors", null, null, List.of(), "NBA"), List.of(10L));
        saga.setId(id.toString());
        saga.setStatus(TeamSagaStatus.COMPLETED);
        saga.setTeamId(55L);
        when(sagaRepository.findById(id.toString())).thenReturn(Optional.of(saga));

        TeamSagaResponse response = service.getStatus(id);

        assertEquals(id, response.sagaId());
        assertEquals(TeamSagaStatus.COMPLETED, response.status());
        assertEquals(55L, response.teamId());
    }

    private Player player(Long id) {
        Player player = mock(Player.class);
        when(player.getId()).thenReturn(id);
        return player;
    }
}
