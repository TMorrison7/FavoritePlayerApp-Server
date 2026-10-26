package app.repositories;

import app.models.TeamSagaPlayerStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamSagaPlayerStepRepository extends JpaRepository<TeamSagaPlayerStep, Long> {
    boolean existsBySagaIdAndPlayerId(String sagaId, Long playerId);
    Optional<TeamSagaPlayerStep> findTopBySagaIdOrderByIdDesc(String sagaId);
}
