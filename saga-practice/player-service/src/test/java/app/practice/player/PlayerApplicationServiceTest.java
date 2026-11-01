package app.practice.player;

import app.practice.contracts.AssignPlayerCommand;
import app.practice.contracts.CreatePlayerRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PlayerApplicationServiceTest {

    @Test
    void assignmentIsIdempotentAndCompensationRestoresPreviousTeam() {
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        PlayerAssignmentStepRepository stepRepository = mock(PlayerAssignmentStepRepository.class);
        PlayerApplicationService service = new PlayerApplicationService(playerRepository, stepRepository);
        PlayerEntity player = new PlayerEntity("Maya", "Moore", "Basketball");
        PlayerEntity secondPlayer = new PlayerEntity("Jordan", "Lee", "Football");
        org.springframework.test.util.ReflectionTestUtils.setField(player, "id", 5L);
        org.springframework.test.util.ReflectionTestUtils.setField(secondPlayer, "id", 6L);
        player.setTeamId(3L);
        secondPlayer.setTeamId(4L);
        when(stepRepository.findById("saga-1:5")).thenReturn(Optional.empty())
                .thenReturn(Optional.of(new PlayerAssignmentStep("saga-1", 5L, 3L, 20L, 0)));
        when(stepRepository.findById("saga-1:6")).thenReturn(Optional.empty())
                .thenReturn(Optional.of(new PlayerAssignmentStep("saga-1", 6L, 4L, 20L, 1)));
        when(playerRepository.findById(5L)).thenReturn(Optional.of(player));
        when(playerRepository.findById(6L)).thenReturn(Optional.of(secondPlayer));

        service.assign(new AssignPlayerCommand("saga-1", 20L, 5L, 0));
        service.assign(new AssignPlayerCommand("saga-1", 20L, 5L, 0));
        service.assign(new AssignPlayerCommand("saga-1", 20L, 6L, 1));

        assertEquals(20L, player.getTeamId());
        assertEquals(20L, secondPlayer.getTeamId());
        verify(stepRepository, times(2)).save(any(PlayerAssignmentStep.class));

        when(stepRepository.findAllBySagaIdOrderByStepNumberDesc("saga-1"))
                .thenReturn(List.of(
                        new PlayerAssignmentStep("saga-1", 6L, 4L, 20L, 1),
                        new PlayerAssignmentStep("saga-1", 5L, 3L, 20L, 0)));
        clearInvocations(playerRepository);
        org.mockito.InOrder compensationOrder = inOrder(playerRepository);
        service.compensate("saga-1");

        compensationOrder.verify(playerRepository).findById(6L);
        compensationOrder.verify(playerRepository).findById(5L);
        assertEquals(3L, player.getTeamId());
        assertEquals(4L, secondPlayer.getTeamId());
        verify(stepRepository).deleteAll(anyList());
    }

    @Test
    void rejectsUnknownPlayerWithoutRecordingACompensationStep() {
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        PlayerAssignmentStepRepository stepRepository = mock(PlayerAssignmentStepRepository.class);
        PlayerApplicationService service = new PlayerApplicationService(playerRepository, stepRepository);
        when(stepRepository.findById("saga-1:999")).thenReturn(Optional.empty());
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PlayerNotFoundException.class,
                () -> service.assign(new AssignPlayerCommand("saga-1", 20L, 999L, 0)));

        verify(stepRepository, never()).save(any());
    }

    @Test
    void createsUnassignedPlayers() {
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        PlayerAssignmentStepRepository stepRepository = mock(PlayerAssignmentStepRepository.class);
        PlayerApplicationService service = new PlayerApplicationService(playerRepository, stepRepository);
        when(playerRepository.saveAndFlush(any(PlayerEntity.class))).thenAnswer(invocation -> {
            PlayerEntity player = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(player, "id", 7L);
            return player;
        });

        var response = service.create(new CreatePlayerRequest("Jordan", "Lee", "Football"));

        assertEquals(7L, response.playerId());
        assertNull(response.teamId());
    }
}
