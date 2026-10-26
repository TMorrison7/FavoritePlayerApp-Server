package app.dto.teamdto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;

@JsonTypeName("CollegeTeam")
@EqualsAndHashCode(callSuper = true)
public final class CollegeTeamRequest extends TeamRequest {

}
