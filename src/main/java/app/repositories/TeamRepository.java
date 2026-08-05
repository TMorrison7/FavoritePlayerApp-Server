package app.repositories;

import app.models.abstractEntities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> getTeamByName(String name);
    List<Team> getTeamByCity(String city);
    List<Team> getTeamByState(String state);
    List<Team> getTeamByDivision(String division);
}
