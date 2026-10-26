package app.service;

import app.models.TeamSaga;
import app.models.TeamSagaPlayerStep;
import app.models.TeamSagaStatus;
import app.models.abstractEntities.Player;
import app.models.abstractEntities.Team;
import app.models.entities.College;
import app.models.entities.ProTeam;
import app.repositories.PlayerRepository;
import app.repositories.TeamRepository;
import app.repositories.TeamSagaPlayerStepRepository;
import app.repositories.TeamSagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeamSagaStepServiceTest {

    private TeamSagaRepository sagaRepository;
    private TeamSagaPlayerStepRepository playerStepRepository;
    private TeamRepository teamRepository;
    private PlayerRepository playerRepository;
    private TeamSagaStepService service;

    @BeforeEach
    void setUp() {
        sagaRepository = mock(TeamSagaRepository.class);
        playerStepRepository = mock(TeamSagaPlayerStepRepository.class);
        teamRepository = mock(TeamRepository.class);
        playerRepository = mock(PlayerRepository.class);
        service = new TeamSagaStepService(
                sagaRepository, playerStepRepository, teamRepository, playerRepository);
    }

    @Test
    void createsProfessionalSubtypeAndPersistsTeamIdWithSaga() {
        TeamSaga saga = saga("PRO");
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
        Team team = mock(Team.class);
        when(team.getId()).thenReturn(55L);
        when(teamRepository.saveAndFlush(any(Team.class))).thenReturn(team);

        assertEquals(55L, service.createTeam(saga.getId()));

        verify(teamRepository).saveAndFlush(argThat(ProTeam.class::isInstance));
        assertEquals(55L, saga.getTeamId());
    }

    @Test
    void createsCollegeSubtypeForCollegeSaga() {
        TeamSaga saga = saga("COLLEGE");
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
        Team team = mock(Team.class);
        when(team.getId()).thenReturn(56L);
        when(teamRepository.saveAndFlush(any(Team.class))).thenReturn(team);

        service.createTeam(saga.getId());

        verify(teamRepository).saveAndFlush(argThat(College.class::isInstance));
    }

    @Test
    void assignsPlayerThroughManyToOneTeamRelationshipAndStoresPreviousTeam() {
        TeamSaga saga = saga("PRO");
        saga.setTeamId(55L);
        Player player = mock(Player.class);
        Team oldTeam = mock(Team.class);
        Team newTeam = mock(Team.class);
        when(oldTeam.getId()).thenReturn(12L);
        when(player.getTeam()).thenReturn(oldTeam);
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(teamRepository.findById(55L)).thenReturn(Optional.of(newTeam));

        service.assignPlayer(saga.getId(), 10L);

        verify(player).setTeam(newTeam);
        verify(playerRepository).save(player);
        verify(playerStepRepository).saveAndFlush(argThat(step ->
                step.getSagaId().equals(saga.getId())
                        && step.getPlayerId().equals(10L)
                        && step.getPreviousTeamId().equals(12L)));
    }

    @Test
    void storesNullPriorTeamForUnassignedPlayer() {
        TeamSaga saga = saga("PRO");
        saga.setTeamId(55L);
        Player player = mock(Player.class);
        Team newTeam = mock(Team.class);
        when(player.getTeam()).thenReturn(null);
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(teamRepository.findById(55L)).thenReturn(Optional.of(newTeam));

        service.assignPlayer(saga.getId(), 10L);

        verify(playerStepRepository).saveAndFlush(argThat(step -> step.getPreviousTeamId() == null));
    }

    @Test
    void skipsRepeatedAssignmentStep() {
        TeamSaga saga = saga("PRO");
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
        when(playerStepRepository.existsBySagaIdAndPlayerId(saga.getId(), 10L)).thenReturn(true);

        service.assignPlayer(saga.getId(), 10L);

        verifyNoInteractions(teamRepository, playerRepository);
    }

    @Test
    void restoresPriorTeamAndDeletesCompensationStep() {
        TeamSaga saga = saga("PRO");
        saga.setStatus(TeamSagaStatus.COMPENSATING);
        TeamSagaPlayerStep step = new TeamSagaPlayerStep(saga.getId(), 10L, 12L);
        Player player = mock(Player.class);
        Team previousTeam = mock(Team.class);
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
        when(playerStepRepository.findTopBySagaIdOrderByIdDesc(saga.getId())).thenReturn(Optional.of(step));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(player));
        when(teamRepository.findById(12L)).thenReturn(Optional.of(previousTeam));

        assertEquals(10L, service.compensateNextPlayer(saga.getId()));

        verify(player).setTeam(previousTeam);
        verify(playerRepository).save(player);
        verify(playerStepRepository).delete(step);
    }

    @Test
    void deletesNewTeamAfterAllAssignmentsHaveBeenCompensated() {
        TeamSaga saga = saga("PRO");
        saga.setStatus(TeamSagaStatus.COMPENSATING);
        saga.setTeamId(55L);
        when(sagaRepository.findById(saga.getId())).thenReturn(Optional.of(saga));
        when(playerStepRepository.findTopBySagaIdOrderByIdDesc(saga.getId())).thenReturn(Optional.empty());

        service.finishCompensation(saga.getId());

        verify(teamRepository).deleteById(55L);
        assertNull(saga.getTeamId());
        assertEquals(TeamSagaStatus.FAILED, saga.getStatus());
    }

    private TeamSaga saga(String type) {
        TeamSaga saga = new TeamSaga(
                new app.dto.teamdto.ProTeamRequestDto("Example", "City", "State", List.of("blue"), "Division"),
                List.of(10L));
        saga.setTeamType(type);
        saga.setStatus(TeamSagaStatus.PROCESSING);
        return saga;
    }
}
