package app.repositories;

import app.models.abstractEntities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {

    Optional<Player> findByFirstNameAndLastName(String firstName, String lastName);
    List<Player> findPlayersByFirstName(String firstName);
    List<Player> findPlayersByLastName(String lastName);
    List<Player> findPlayersByBirthState(String state);
    List<Player> findPlayersByBirthCity(String city);
    List<Player> findPlayersByHighSchool(String highSchool);

}
