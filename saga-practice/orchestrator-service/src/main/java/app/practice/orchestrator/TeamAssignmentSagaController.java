package app.practice.orchestrator;

import app.practice.contracts.TeamAssignmentSagaRequest;
import app.practice.contracts.TeamAssignmentSagaResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sagas/team-assignments")
public class TeamAssignmentSagaController {

    private final TeamAssignmentSagaStateService stateService;
    private final TeamAssignmentSagaExecutor executor;

    public TeamAssignmentSagaController(
            TeamAssignmentSagaStateService stateService,
            TeamAssignmentSagaExecutor executor) {
        this.stateService = stateService;
        this.executor = executor;
    }

    @PostMapping
    public TeamAssignmentSagaResponse start(@Valid @RequestBody TeamAssignmentSagaRequest request) {
        String sagaId = stateService.create(request);
        return executor.execute(sagaId);
    }

    @GetMapping("/{sagaId}")
    public TeamAssignmentSagaResponse status(@PathVariable String sagaId) {
        return stateService.response(sagaId);
    }

    @PostMapping("/{sagaId}/resume")
    public TeamAssignmentSagaResponse resume(@PathVariable String sagaId) {
        stateService.prepareResume(sagaId);
        return executor.execute(sagaId);
    }

    @PostMapping("/{sagaId}/retry-compensation")
    public TeamAssignmentSagaResponse retryCompensation(@PathVariable String sagaId) {
        return executor.retryCompensation(sagaId);
    }
}
