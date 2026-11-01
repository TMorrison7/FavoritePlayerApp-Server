package app.practice.contracts;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TeamAssignmentSagaRequest(
        @Valid @NotNull CreateTeamRequest team,
        @NotEmpty List<@NotNull Long> playerIds
) {
}
