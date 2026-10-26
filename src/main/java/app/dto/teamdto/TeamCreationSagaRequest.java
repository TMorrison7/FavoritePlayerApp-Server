package app.dto.teamdto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TeamCreationSagaRequest(
        @Valid @NotNull TeamRequestDto team,
        @NotEmpty List<@NotNull Long> playerIds
) {
}
