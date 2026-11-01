package app.practice.orchestrator;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamAssignmentSagaRepository extends JpaRepository<TeamAssignmentSagaEntity, String> {
}
