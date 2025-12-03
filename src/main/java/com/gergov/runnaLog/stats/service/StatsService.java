package com.gergov.runnaLog.stats.service;

import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.stats.repository.StatsRepository;
import com.gergov.runnaLog.user.model.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class StatsService {


    private final StatsRepository statsRepository;
    private final RunRepository runRepository;


    @Autowired
    public StatsService(StatsRepository statsRepository, RunRepository runRepository) {
        this.statsRepository = statsRepository;
        this.runRepository = runRepository;
    }

    @Transactional
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

    @Transactional
    public void createDefaultStatsForDefaultUser(User user) {

        Stats stats = Stats.builder()
                .user(user)
                .totalRuns(0)
                .totalDistance(0.0)
                .totalDuration(0)
                .pb1km(null)
                .pb5km(null)
                .pb10km(null)
                .runnerLevel(1)
                .strides(6425)
                .lastActivity(LocalDateTime.now())
                .build();

        statsRepository.save(stats);

    }

    public void updateUserStatsAfterRun(User user, Double distance, long totalSeconds, String pace) {

        Stats stats = user.getStats();
        stats.setTotalRuns(stats.getTotalRuns() + 1);
        stats.setTotalDistance(stats.getTotalDistance() + distance);
        stats.setTotalDuration((int) (stats.getTotalDuration() + totalSeconds/60));
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

    private Integer parsePaceToSeconds(String paceString) {

        String[] parts = paceString.split(":");

        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);

        return (minutes * 60) + seconds;
    }

    private void updateStridesEarned(Stats stats, Double distance, Integer totalSeconds) {

        int stridesEarned = (int) (distance * 10);

        double paceMinPerKm = (totalSeconds / 60.0) / distance;

        if (paceMinPerKm < 4.0) {
            stridesEarned += 50;
        }
        else if (paceMinPerKm < 5.0) {
            stridesEarned += 30;
        }
        else if (paceMinPerKm < 6.0) {
            stridesEarned += 15;
        }

        if (distance >= 21.1) {
            stridesEarned += 100;
        }
        else if (distance >= 10.0) {
            stridesEarned += 50;
        }
        else if (distance >= 5.0) {
            stridesEarned += 20;
        }

        stats.setStrides(stats.getStrides() + stridesEarned);
        log.debug("User earned [%d] strides for this run".formatted(stridesEarned));
    }

    public void updateUserStatsAfterDeleteRun(User user, Double distance, long totalSeconds) {
        Stats stats = user.getStats();

        int totalRuns = stats.getTotalRuns() - 1;
        if (totalRuns < 0) {
            stats.setTotalRuns(0);
        }
        stats.setTotalRuns(stats.getTotalRuns() - 1);

        double totalDistance = stats.getTotalDistance() - distance;
        if (totalDistance < 0) {
            stats.setTotalDistance(0.0);
        }
        stats.setTotalDistance(stats.getTotalDistance() - distance);

        Duration totalDuration = Duration.ofMinutes(stats.getTotalDuration() - (totalSeconds/60));
        if (totalDuration.isNegative()) {
            stats.setTotalDuration(0);
        }
        stats.setTotalDuration((int) (stats.getTotalDuration() - totalSeconds/60));

        stats.setLastActivity(LocalDateTime.now());

        updatePersonalBestsAfterDeleteRun(stats, user);
        updateStridesEarnedAfterDelete(stats, distance, (int) totalSeconds);

        statsRepository.save(stats);
    }

    private void updateStridesEarnedAfterDelete(Stats stats, Double distance, int totalSeconds) {

        int stridesLost = (int) (distance * 10);

        double paceMinPerKm = (totalSeconds / 60.0) / distance;

        if (paceMinPerKm < 4.0) stridesLost += 50;
        else if (paceMinPerKm < 5.0) stridesLost += 30;
        else if (paceMinPerKm < 6.0) stridesLost += 15;

        if (distance >= 21.1) stridesLost += 100;
        else if (distance >= 10.0) stridesLost += 50;
        else if (distance >= 5.0) stridesLost += 20;

        int balance = stats.getStrides() - stridesLost;
        if (balance < 0) {
            stats.setStrides(0);
        } else {
            stats.setStrides(stats.getStrides() - stridesLost);
        }

        log.debug("User left with [%d] strides after deleting this run".formatted(balance));
    }

    private void updatePersonalBestsAfterDeleteRun(Stats stats, User user) {

        List<Run> runs = runRepository.findPublicRunsByUserOrderByPaceDesc(user);

        if (runs.isEmpty()) {
            stats.setPb1km(null);
            stats.setPb5km(null);
            stats.setPb10km(null);
        } else {
            Run run = runs.get(runs.size() - 1);
            String fastestPace = run.getPace();

            int pb1PaceSeconds = parsePaceToSeconds(stats.getPb1km());
            int pb5PaceSeconds = parsePaceToSeconds(stats.getPb5km());
            int pb10PaceSeconds = parsePaceToSeconds(stats.getPb10km());
            int runPaceSeconds = parsePaceToSeconds(fastestPace);

            if (runPaceSeconds > pb1PaceSeconds) {
                stats.setPb1km(fastestPace);
            }

            if (runPaceSeconds > pb5PaceSeconds && run.getDistance() >= 5.00) {
                stats.setPb5km(fastestPace);
            }

            if (runPaceSeconds > pb10PaceSeconds && run.getDistance() >= 10.00) {
                stats.setPb10km(fastestPace);
            }
        }

    }
}
