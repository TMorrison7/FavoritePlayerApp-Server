package app.practice.team;

import app.practice.contracts.CreateTeamCommand;
import app.practice.contracts.CreateTeamRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TeamApplicationServiceTest {

    @Test
    void createsTeamOnceForSagaId() {
        TeamRepository repository = mock(TeamRepository.class);
        TeamApplicationService service = new TeamApplicationService(repository);
        CreateTeamCommand command = new CreateTeamCommand(
                "saga-1",
                new CreateTeamRequest("Example", "City", "CA", "Division", List.of("blue"), "PRO"));
        TeamEntity createdTeam = new TeamEntity(
                "saga-1", "Example", "City", "CA", "Division", List.of("blue"), "PRO");
        org.springframework.test.util.ReflectionTestUtils.setField(createdTeam, "id", 12L);
        when(repository.findBySagaId("saga-1")).thenReturn(Optional.empty(), Optional.of(createdTeam));
        when(repository.saveAndFlush(any(TeamEntity.class))).thenReturn(createdTeam);

        var firstResponse = service.create(command);
        var retryResponse = service.create(command);

        assertEquals(12L, firstResponse.teamId());
        assertEquals(firstResponse, retryResponse);
        verify(repository, times(2)).findBySagaId("saga-1");
        verify(repository, times(1)).saveAndFlush(any(TeamEntity.class));
    }

    @Test
    void compensationIsSafeWhenRetried() {
        TeamRepository repository = mock(TeamRepository.class);
        TeamApplicationService service = new TeamApplicationService(repository);
        when(repository.findBySagaId("saga-1")).thenReturn(Optional.empty());

        service.compensateCreate("saga-1");
        service.compensateCreate("saga-1");

        verify(repository, times(2)).findBySagaId("saga-1");
        verify(repository, never()).delete(any());
    }
}
