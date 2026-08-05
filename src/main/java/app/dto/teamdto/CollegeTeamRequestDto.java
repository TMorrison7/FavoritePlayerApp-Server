package app.dto.teamdto;

import java.util.List;

public final class CollegeTeamRequestDto extends TeamRequestDto {
    public CollegeTeamRequestDto(String name, String city, String state, List<String> colors, String division) {
        super(name, city, state, colors, division);
    }
}
