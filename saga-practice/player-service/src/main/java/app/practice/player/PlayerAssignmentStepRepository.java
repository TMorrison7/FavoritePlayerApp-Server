package app.practice.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerAssignmentStepRepository extends JpaRepository<PlayerAssignmentStep, String> {
    List<PlayerAssignmentStep> findAllBySagaIdOrderByStepNumberDesc(String sagaId);
}
