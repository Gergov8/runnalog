package com.gergov.runnaLog.stats.service;

import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.stats.repository.StatsRepository;
import com.gergov.runnaLog.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
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
                .totalDuration(0)
                .pb1km(null)
                .pb5km(null)
                .pb10km(null)
                .runnerLevel(1)
                .strides(100)
                .lastActivity(LocalDateTime.now())
                .build();

        statsRepository.save(stats);

    }

    public void updateUserStatsAfterRun(User user, Double distance, long totalSeconds, String pace) {
        Stats stats = user.getStats();
        stats.setTotalRuns(stats.getTotalRuns() + 1);
        stats.setTotalDistance(stats.getTotalDistance() + distance);
        stats.setTotalDuration((int) (stats.getTotalDuration() + totalSeconds));
        stats.setLastActivity(LocalDateTime.now());

        updatePersonalBests(stats, distance, pace);
        updateStridesEarned(stats, distance, (int) totalSeconds);

        statsRepository.save(stats);
    }

    private void updatePersonalBests(Stats stats, Double distance, String pace) {

        int paceSeconds = parsePaceToSeconds(pace);

        if (distance >= 1.0) {
            if (stats.getPb1km() == null || paceSeconds < parsePaceToSeconds(stats.getPb1km())) {
                stats.setPb1km(pace);
                log.debug("New 1km personal best: {} min/km", pace);
            }
        }


        if (distance >= 5.0) {
            if (stats.getPb5km() == null || paceSeconds < parsePaceToSeconds(stats.getPb5km())) {
                stats.setPb5km(pace);
                log.debug("New 5km personal best: {} min/km", pace);
            }
        }


        if (distance >= 10.0) {
            if (stats.getPb10km() == null || paceSeconds < parsePaceToSeconds(stats.getPb10km())) {
                stats.setPb10km(pace);
                log.debug("New 10km personal best: {} min/km", pace);
            }
        }
    }

    // Получава темпо в секунди на ккм (за изчисления) (извлечено от формат "3:30" и превърнато в 220сек.)
    private Integer parsePaceToSeconds(String paceString) {

        String[] parts = paceString.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return (minutes * 60) + seconds;
    }

    private void updateStridesEarned(Stats stats, Double distance, Integer totalSeconds) {
        // Основно вънаграждение -> 10 strides на км
        int stridesEarned = (int) (distance * 10);

        double paceMinPerKm = (totalSeconds / 60.0) / distance;

        // Бонус за скорост -> по-бързо темпо = повече strides
        if (paceMinPerKm < 4.0) stridesEarned += 50;  // Elite pace (< 4:00 min/km)
        else if (paceMinPerKm < 5.0) stridesEarned += 30;  // Fast pace (< 5:00 min/km)
        else if (paceMinPerKm < 6.0) stridesEarned += 15;  // Average pace (< 6:00 min/km)

        // Бонус за дистанция
        if (distance >= 21.1) stridesEarned += 100;  // Half-marathon
        else if (distance >= 10.0) stridesEarned += 50;  // 10k+
        else if (distance >= 5.0) stridesEarned += 20;  // 5k+

        stats.setStrides(stats.getStrides() + stridesEarned);
        log.debug("User earned [%d] strides for this run".formatted(stridesEarned));
    }
}
