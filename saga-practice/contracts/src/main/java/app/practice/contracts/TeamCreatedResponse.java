package app.practice.contracts;

public record TeamCreatedResponse(
        Long teamId,
        String name,
        String teamType
) {
}
