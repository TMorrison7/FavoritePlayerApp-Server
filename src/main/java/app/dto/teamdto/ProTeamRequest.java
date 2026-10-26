package app.dto.teamdto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@JsonTypeName("ProTeam")
@EqualsAndHashCode(callSuper = true)
public final class ProTeamRequest extends TeamRequest {

}
