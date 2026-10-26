package app.models;

import app.models.abstractEntities.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorite_user_player",
                columnNames = {"user_id", "player_id"}
        )
)
@Getter
@NoArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    public Favorite(User user, Player player) {
        this.user = user;
        this.player = player;
    }
}
