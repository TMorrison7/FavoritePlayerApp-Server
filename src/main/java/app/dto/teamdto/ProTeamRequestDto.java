package app.dto.teamdto;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public final class ProTeamRequestDto extends TeamRequestDto {
    public ProTeamRequestDto(String name, String city, String state, List<String> colors, String division) {
        super(name, city, state, colors, division);
    }
}
