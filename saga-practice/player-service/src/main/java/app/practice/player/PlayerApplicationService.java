package app.practice.player;

import app.practice.contracts.AssignPlayerCommand;
import app.practice.contracts.CreatePlayerRequest;
import app.practice.contracts.PlayerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerApplicationService {

    private final PlayerRepository playerRepository;
    private final PlayerAssignmentStepRepository stepRepository;

    public PlayerApplicationService(
            PlayerRepository playerRepository,
            PlayerAssignmentStepRepository stepRepository) {
        this.playerRepository = playerRepository;
        this.stepRepository = stepRepository;
    }

    @Transactional
    public PlayerResponse create(CreatePlayerRequest request) {
        if (request.firstName() == null || request.firstName().isBlank()
                || request.lastName() == null || request.lastName().isBlank()
                || request.sport() == null || request.sport().isBlank()) {
            throw new IllegalArgumentException("First name, last name, and sport are required.");
        }
        return response(playerRepository.saveAndFlush(
                new PlayerEntity(request.firstName(), request.lastName(), request.sport())));
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> findAll() {
        return playerRepository.findAll().stream().map(this::response).toList();
    }

    @Transactional
    public void assign(AssignPlayerCommand command) {
        String stepId = command.sagaId() + ":" + command.playerId();
        PlayerAssignmentStep existingStep = stepRepository.findById(stepId).orElse(null);
        if (existingStep != null) {
            if (!existingStep.getAssignedTeamId().equals(command.teamId())) {
                throw new IllegalStateException("Saga ID was already used for a different player assignment.");
            }
            return;
        }

        PlayerEntity player = playerRepository.findById(command.playerId())
                .orElseThrow(PlayerNotFoundException::new);
        stepRepository.save(new PlayerAssignmentStep(
                command.sagaId(),
                player.getId(),
                player.getTeamId(),
                command.teamId(),
                command.stepNumber()));
        player.setTeamId(command.teamId());
    }

    @Transactional
    public void compensate(String sagaId) {
        List<PlayerAssignmentStep> steps = stepRepository.findAllBySagaIdOrderByStepNumberDesc(sagaId);
        if (steps.isEmpty()) {
            return;
        }

        for (PlayerAssignmentStep step : steps) {
            PlayerEntity player = playerRepository.findById(step.getPlayerId())
                    .orElseThrow(PlayerNotFoundException::new);
            player.setTeamId(step.getPreviousTeamId());
        }
        stepRepository.deleteAll(steps);
    }

    private PlayerResponse response(PlayerEntity player) {
        return new PlayerResponse(
                player.getId(),
                player.getFirstName(),
                player.getLastName(),
                player.getSport(),
                player.getTeamId()
        );
    }
}
