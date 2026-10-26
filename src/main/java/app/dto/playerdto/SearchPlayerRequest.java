package app.dto.playerdto;




import app.models.abstractEntities.Team;

import java.time.LocalDate;

public record SearchPlayerRequest(
        String firstName, String lastName, LocalDate dateOfBirth, String birthState,
        String birthCity, Team team, String highSchool)
{

}