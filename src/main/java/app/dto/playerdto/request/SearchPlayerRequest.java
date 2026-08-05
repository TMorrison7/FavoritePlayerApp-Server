package app.dto.playerdto.request;


import app.models.abstractEntities.Team;
import app.models.entities.College;

import java.time.LocalDate;

public record SearchPlayerRequest(
        String firstName, String lastName, LocalDate dateOfBirth, String birthState
        ,String birthCity, Team team, String highSchool, College college)
{}