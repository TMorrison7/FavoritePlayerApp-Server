package app.dto.playerdto.request;

import app.models.abstractEntities.Team;
import app.models.entities.College;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

// Extendable Polymorphic Firewall
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "sport"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BasketballCreateRequest.class, name = "BASKETBALL"),
        @JsonSubTypes.Type(value = FootballCreateRequest.class, name = "FOOTBALL")
})

@Data
@RequiredArgsConstructor // Generates the constructor for final fields
@EqualsAndHashCode // Essential for comparing immutable objects safely
public sealed abstract class PlayerCreateRequest permits BasketballCreateRequest, FootballCreateRequest {

    @NotBlank(message = "First name is required")
    private final String firstName;
    @NotBlank(message = "Last name is required")
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String birthState;
    private final String birthCity;
    private final Team team;
    private final String highSchool;
    private final College college;
    private final String sport;
}

