package app.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "team_saga_player_steps",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_team_saga_player_step",
                columnNames = {"saga_id", "player_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class TeamSagaPlayerStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_id", nullable = false, length = 36)
    private String sagaId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "previous_team_id")
    private Long previousTeamId;

    public TeamSagaPlayerStep(String sagaId, Long playerId, Long previousTeamId) {
        this.sagaId = sagaId;
        this.playerId = playerId;
        this.previousTeamId = previousTeamId;
    }
}
