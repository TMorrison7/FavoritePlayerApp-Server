package app.dto.teamdto;

import java.util.List;

public record SearchTeamRequest(
        String name, String city, String state, List<String> colors, String division
) {
}
