package app.dto;

import app.models.abstractEntities.Team;
import app.models.entities.College;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

// Lombok automatically hooks into the parent constructor using super(name)
@Getter
@EqualsAndHashCode(callSuper = true)
public final class BasketballPlayerRequestDto extends PlayerRequestDto {
    public BasketballPlayerRequestDto(String firstName, String lastName, LocalDate dateOfBirth, String birthState
            ,String birthCity, Team team, String highSchool, College college){

        super(firstName,lastName,dateOfBirth,birthState,birthCity,
                team, highSchool, college);
    }
}