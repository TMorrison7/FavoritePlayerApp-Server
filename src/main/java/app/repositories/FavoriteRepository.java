package app.repositories;

import app.models.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findAllByUser_UsernameOrderById(String username);
    Optional<Favorite> findByUser_UsernameAndPlayer_Id(String username, Long playerId);
    void deleteByUser_UsernameAndPlayer_Id(String username, Long playerId);
}
