package app.dto.playerdto.request;

import app.models.abstractEntities.Team;
import app.models.entities.College;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

// Lombok automatically hooks into the parent constructor using super(name)

@JsonTypeName("FOOTBALL")
@EqualsAndHashCode(callSuper = true)
public final class FootballCreateRequest extends PlayerCreateRequest {


    public FootballCreateRequest(String firstName, String lastName, LocalDate dateOfBirth, String birthState, String birthCity, Team team, String highSchool, College college, String sport) {
        super(firstName, lastName, dateOfBirth, birthState, birthCity, team, highSchool, college, sport = "FOOTBALL");
    }
}
