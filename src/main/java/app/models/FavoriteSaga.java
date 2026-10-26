package app.models;

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
@Table(name = "favorite_sagas")
@Getter
@Setter
@NoArgsConstructor
public class FavoriteSaga {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FavoriteSagaStatus status;

    @ElementCollection
    @CollectionTable(name = "favorite_saga_players", joinColumns = @JoinColumn(name = "saga_id"))
    @OrderColumn(name = "player_order")
    @Column(name = "player_id", nullable = false)
    private List<Long> playerIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "favorite_saga_completed_players", joinColumns = @JoinColumn(name = "saga_id"))
    @OrderColumn(name = "completion_order")
    @Column(name = "player_id", nullable = false)
    private List<Long> completedPlayerIds = new ArrayList<>();

    @Column(length = 2000)
    private String failureMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public FavoriteSaga(String username, List<Long> playerIds) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.status = FavoriteSagaStatus.PENDING;
        this.playerIds = new ArrayList<>(playerIds);
        this.createdAt = Instant.now();
    }
}
