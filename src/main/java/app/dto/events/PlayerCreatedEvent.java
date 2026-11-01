package app.dto.events;

public record PlayerCreatedEvent(
        Long playerId,
        String firstName,
        String lastName,
        String sport,
        Long teamId
) {
}
