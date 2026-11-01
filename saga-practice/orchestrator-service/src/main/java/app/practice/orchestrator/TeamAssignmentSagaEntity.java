package app.practice.orchestrator;

import app.practice.contracts.SagaStatus;
import app.practice.contracts.TeamAssignmentSagaRequest;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "team_assignment_sagas")
public class TeamAssignmentSagaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SagaStatus status;

    @Column(nullable = false)
    private String teamName;

    private String city;
    private String state;
    private String division;
    private String teamType;

    @ElementCollection
    @CollectionTable(name = "saga_team_colors", joinColumns = @JoinColumn(name = "saga_id"))
    @OrderColumn(name = "color_order")
    @Column(name = "color")
    private List<String> colors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "saga_player_ids", joinColumns = @JoinColumn(name = "saga_id"))
    @OrderColumn(name = "player_order")
    @Column(name = "player_id", nullable = false)
    private List<Long> playerIds = new ArrayList<>();

    private Long teamId;

    @Column(length = 2000)
    private String failureMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected TeamAssignmentSagaEntity() {
    }

    public TeamAssignmentSagaEntity(TeamAssignmentSagaRequest request) {
        this.id = UUID.randomUUID().toString();
        this.status = SagaStatus.PENDING;
        this.teamName = request.team().name();
        this.city = request.team().city();
        this.state = request.team().state();
        this.division = request.team().division();
        this.teamType = request.team().teamType();
        this.colors = request.team().colors() == null
                ? new ArrayList<>()
                : new ArrayList<>(request.team().colors());
        this.playerIds = request.playerIds().stream().distinct().toList();
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getDivision() {
        return division;
    }

    public String getTeamType() {
        return teamType;
    }

    public List<String> getColors() {
        return List.copyOf(colors);
    }

    public List<Long> getPlayerIds() {
        return List.copyOf(playerIds);
    }

    public Long getTeamId() {
        return teamId;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    void setStatus(SagaStatus status) {
        this.status = status;
    }

    void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage == null
                ? null
                : failureMessage.substring(0, Math.min(failureMessage.length(), 2000));
    }
}
