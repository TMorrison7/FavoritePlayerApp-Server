package app.models.abstractEntities;

import lombok.*;
import app.models.entities.College;
import jakarta.persistence.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "players")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "sport")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public abstract class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String birthState;
    private String birthCity;
    private Team team;
    private String highSchool;
    private College college;

}
