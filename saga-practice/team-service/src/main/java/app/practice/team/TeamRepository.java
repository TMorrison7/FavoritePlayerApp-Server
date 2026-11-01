package app.practice.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
    Optional<TeamEntity> findBySagaId(String sagaId);
}
