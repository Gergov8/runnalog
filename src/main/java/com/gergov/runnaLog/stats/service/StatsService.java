package com.gergov.runnaLog.stats.service;

import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.stats.repository.StatsRepository;
import com.gergov.runnaLog.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StatsService {


    private final StatsRepository statsRepository;

    @Autowired
    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public void createDefaultStats(User user) {

        Stats stats = Stats.builder()
                .user(user)
                .totalRuns(0)
                .totalDistance(0.0)
                .totalDuration(0.0)
                .pb1km(null)
                .pb5km(null)
                .pb10km(null)
                .runnerLevel(1)
                .strides(100)
                .lastActivity(LocalDateTime.now())
                .build();

        statsRepository.save(stats);

    }
}
