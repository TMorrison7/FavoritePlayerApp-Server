package app.practice.contracts;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTeamCommand(
        @NotBlank String sagaId,
        @Valid @NotNull CreateTeamRequest team
) {
}
