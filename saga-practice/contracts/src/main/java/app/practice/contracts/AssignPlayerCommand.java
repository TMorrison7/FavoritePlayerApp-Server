package app.practice.contracts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record AssignPlayerCommand(
        @NotBlank String sagaId,
        @NotNull Long teamId,
        @NotNull Long playerId,
        @Min(0) int stepNumber
) {
}
