package app.dto.playerdto;

import app.models.abstractEntities.Team;
import app.models.entities.College;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

// Extendable Polymorphic Firewall
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "sport",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BasketballRequest.class, name = "Basketball"),
        @JsonSubTypes.Type(value = FootballRequest.class, name = "Football")
})

@Data
@RequiredArgsConstructor // Generates the constructor for final fields
@EqualsAndHashCode // Essential for comparing immutable objects safely
public sealed abstract class PlayerRequest permits BasketballRequest, FootballRequest {
    private Long id;
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    private LocalDate dateOfBirth;
    private String birthState;
    private String birthCity;
    private Team team;
    private String highSchool;
    private College college;
    private String sport;
}

