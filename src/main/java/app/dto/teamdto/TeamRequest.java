package app.dto.teamdto;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "level",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProTeamRequest.class, name = "ProTeam"),
        @JsonSubTypes.Type(value = CollegeTeamRequest.class, name = "CollegeTeam")
})


@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public sealed abstract class TeamRequest permits ProTeamRequest, CollegeTeamRequest {

    private Long id;
    @NotBlank(message = "Team name is required")
    private String name;
    private String city;
    private String state;
    private List<String> colors;
    private String division;

}
