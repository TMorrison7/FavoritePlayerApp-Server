package app.practice.contracts;

public record PlayerResponse(
        Long playerId,
        String firstName,
        String lastName,
        String sport,
        Long teamId
) {
}
