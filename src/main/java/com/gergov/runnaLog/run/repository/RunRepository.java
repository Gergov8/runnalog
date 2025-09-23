package com.gergov.runnaLog.run.repository;

import com.gergov.runnaLog.run.model.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RunRepository extends JpaRepository<Run, UUID> {
}
