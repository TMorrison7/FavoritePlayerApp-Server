package app.dto;

import app.models.abstractEntities.Team;
import app.models.entities.College;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
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
        @JsonSubTypes.Type(value = FootballPlayerRequestDto.class, name = "football"),
        @JsonSubTypes.Type(value = BasketballPlayerRequestDto.class, name = "basketball")
})

@Getter
@RequiredArgsConstructor // Generates the constructor for final fields
@EqualsAndHashCode // Essential for comparing immutable objects safely
public sealed abstract class PlayerRequestDto permits BasketballPlayerRequestDto, FootballPlayerRequestDto{

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
}

