package app.dto.teamdto;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "level"

)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProTeamRequestDto.class, name = "proTeam"),
        @JsonSubTypes.Type(value = CollegeTeamRequestDto.class, name = "collegeTeam")
})


@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public sealed abstract class TeamRequestDto permits ProTeamRequestDto, CollegeTeamRequestDto {

    @NotBlank(message = "Team name is required")
    private final String name;
    private final String city;
    private final String state;
    private final List<String> colors;
    private final String division;

}
