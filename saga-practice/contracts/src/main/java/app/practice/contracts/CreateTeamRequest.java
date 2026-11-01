package app.practice.contracts;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateTeamRequest(
        @NotBlank String name,
        String city,
        String state,
        String division,
        List<String> colors,
        String teamType
) {
}
