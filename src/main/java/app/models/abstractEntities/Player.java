package app.models.abstractEntities;

import lombok.*;
import app.models.entities.College;
import jakarta.persistence.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "players")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "sport", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public abstract class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    private LocalDate dateOfBirth;
    private String birthState;
    private String birthCity;
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
    private String highSchool;
    @Column(name = "sport", insertable = false, updatable = false)
    private String sport;
}
