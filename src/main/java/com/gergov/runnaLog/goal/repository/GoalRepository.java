package com.gergov.runnaLog.goal.repository;

import com.gergov.runnaLog.goal.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {
}
