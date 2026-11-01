package app.practice.team;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String sagaId;

    @Column(nullable = false)
    private String name;

    private String city;
    private String state;
    private String division;

    @ElementCollection
    @CollectionTable(name = "team_colors", joinColumns = @JoinColumn(name = "team_id"))
    @OrderColumn(name = "color_order")
    @Column(name = "color", nullable = false)
    private List<String> colors = new ArrayList<>();

    @Column(nullable = false, length = 16)
    private String teamType;

    protected TeamEntity() {
    }

    public TeamEntity(String sagaId, String name, String city, String state,
                      String division, List<String> colors, String teamType) {
        this.sagaId = sagaId;
        this.name = name;
        this.city = city;
        this.state = state;
        this.division = division;
        this.colors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
        this.teamType = teamType;
    }

    public Long getId() {
        return id;
    }

    public String getSagaId() {
        return sagaId;
    }

    public String getName() {
        return name;
    }

    public String getTeamType() {
        return teamType;
    }
}
