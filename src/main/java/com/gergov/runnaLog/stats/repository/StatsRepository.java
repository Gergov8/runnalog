package com.gergov.runnaLog.stats.repository;

import com.gergov.runnaLog.stats.model.Stats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StatsRepository extends JpaRepository<Stats, UUID> {
}
