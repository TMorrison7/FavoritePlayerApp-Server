package app.practice.player;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_assignment_steps")
public class PlayerAssignmentStep {

    @Id
    @Column(length = 73)
    private String id;

    @Column(nullable = false, length = 36)
    private String sagaId;

    @Column(nullable = false)
    private Long playerId;

    private Long previousTeamId;

    @Column(nullable = false)
    private Long assignedTeamId;

    @Column(nullable = false)
    private int stepNumber;

    protected PlayerAssignmentStep() {
    }

    public PlayerAssignmentStep(
            String sagaId, Long playerId, Long previousTeamId, Long assignedTeamId, int stepNumber) {
        this.id = sagaId + ":" + playerId;
        this.sagaId = sagaId;
        this.playerId = playerId;
        this.previousTeamId = previousTeamId;
        this.assignedTeamId = assignedTeamId;
        this.stepNumber = stepNumber;
    }

    public String getSagaId() {
        return sagaId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public Long getPreviousTeamId() {
        return previousTeamId;
    }

    public Long getAssignedTeamId() {
        return assignedTeamId;
    }
}
