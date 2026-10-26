package app.models;

import app.dto.teamdto.TeamRequest;
import app.dto.teamdto.CollegeTeamRequest;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "team_sagas")
@Getter
@Setter
@NoArgsConstructor
public class TeamSaga {

    @Id
    @Column(length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamSagaStatus status;

    @Column(nullable = false, length = 20)
    private String teamType;

    @Column(nullable = false)
    private String teamName;

    private String city;
    private String state;
    private String division;

    @ElementCollection
    @CollectionTable(name = "team_saga_colors", joinColumns = @JoinColumn(name = "saga_id"))
    @OrderColumn(name = "color_order")
    @Column(name = "color")
    private List<String> colors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "team_saga_players", joinColumns = @JoinColumn(name = "saga_id"))
    @OrderColumn(name = "player_order")
    @Column(name = "player_id", nullable = false)
    private List<Long> playerIds = new ArrayList<>();

    private Long teamId;

    @Column(length = 2000)
    private String failureMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public TeamSaga(TeamRequest request, List<Long> playerIds) {
        this.id = UUID.randomUUID().toString();
        this.status = TeamSagaStatus.PENDING;
        this.teamType = request instanceof CollegeTeamRequest ? "COLLEGE" : "PRO";
        this.teamName = request.getName();
        this.city = request.getCity();
        this.state = request.getState();
        this.division = request.getDivision();
        this.colors = request.getColors() == null
                ? new ArrayList<>()
                : new ArrayList<>(request.getColors());
        this.playerIds = new ArrayList<>(playerIds);
        this.createdAt = Instant.now();
    }
}
