package app.models.abstractEntities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "teams")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "level")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public abstract class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @Column(nullable = false)
    private String name;
    private String city;
    private String state;
    private List<String> colors;
    private String division;
    private String level;

}
