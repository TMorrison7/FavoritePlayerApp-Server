package app.dto.userdto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FavoritePlayersRequest(@NotEmpty List<@NotNull Long> playerIds) {
}
