package app.repositories;

import app.models.FavoriteSaga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteSagaRepository extends JpaRepository<FavoriteSaga, String> {
    Optional<FavoriteSaga> findByIdAndUsername(String id, String username);
}
