package app.dto.events;

public record TeamCreatedEvent(
        Long teamId,
        String name,
        String city,
        String state,
        String division,
        String teamType
) {
}
