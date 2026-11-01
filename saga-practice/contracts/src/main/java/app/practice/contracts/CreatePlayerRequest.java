package app.practice.contracts;

import jakarta.validation.constraints.NotBlank;

public record CreatePlayerRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String sport
) {
}
