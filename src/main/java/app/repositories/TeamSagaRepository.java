package app.repositories;

import app.models.TeamSaga;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamSagaRepository extends JpaRepository<TeamSaga, String> {
}
