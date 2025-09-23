package com.gergov.runnaLog.run.service;

import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.model.RunVisibility;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.stats.model.Stats;
import com.gergov.runnaLog.stats.repository.StatsRepository;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RunService {


    private final RunRepository runRepository;
    private final StatsRepository statsRepository;

    @Autowired
    public RunService(RunRepository runRepository, StatsRepository statsRepository) {
        this.runRepository = runRepository;
        this.statsRepository = statsRepository;
    }

    public Run createRun(CreateRunRequest createRunRequest, User user) {

        // Смята пълното време в секунди
        Integer totalSeconds = calculateTotalSeconds(createRunRequest.hours(), createRunRequest.minutes(), createRunRequest.seconds());

        // Изчислява темпо в секунди на километър, след което форматира като MM:SS
        String pace = calculatePace(totalSeconds, createRunRequest.distance());

        Run run = Run.builder()
                .distance(createRunRequest.distance())
                .hours(createRunRequest.hours())
                .minutes(createRunRequest.minutes())
                .seconds(createRunRequest.seconds())
                .pace(pace)
                .title(createRunRequest.title())
                .description(createRunRequest.description())
                .createdOn(LocalDateTime.now())
                .visibility(createRunRequest.visibility())
                .user(user)
                .build();

        Run savedRun =  runRepository.save(run);

        updateUserStats(user, createRunRequest.distance(), totalSeconds, pace);

        log.info("User [%s] created a run of [%.2f] km in [%d]:[%d]:[%d]".formatted(user.getUsername(), createRunRequest.distance(),
                createRunRequest.hours(), createRunRequest.minutes(), createRunRequest.seconds()));
        return savedRun;
    }

    private Integer calculateTotalSeconds(Integer hours, Integer minutes, Integer seconds) {
        return (hours * 3600) + (minutes * 60) + seconds;
    }

    private String calculatePace(Integer totalSeconds, Double distance) {

        // Темпо в секунди на км
        int paceSeconds = (int) (totalSeconds / distance);

        // Форматирани в MM:SS
        int paceMinutes = paceSeconds / 60;
        int paceSecondsRemainder = paceSeconds % 60;

        return String.format("%d:%02d", paceMinutes, paceSecondsRemainder);
    }


//    public String formatDuration(Run run) {
//
//        if (run.getHours() > 0) {
//            return String.format("%d:%02d:%02d", run.getHours(), run.getMinutes(), run.getSeconds());
//        } else {
//            return String.format("%d:%02d", run.getMinutes(), run.getSeconds());
//        }
//
//    }

    // Получава темпо в секунди (за изчисления) (извлечено от формат "5:30")
    private Integer parsePaceToSeconds(String paceString) {

        String[] parts = paceString.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return (minutes * 60) + seconds;
    }


    private void updateUserStats(User user, Double distance, Integer totalSeconds, String pace) {
        Stats stats = user.getStats();
        stats.setTotalRuns(stats.getTotalRuns() + 1);
        stats.setTotalDistance(stats.getTotalDistance() + distance);
        stats.setTotalDuration(stats.getTotalDuration() + totalSeconds);
        stats.setLastActivity(LocalDateTime.now());

        updatePersonalBests(stats, distance, pace);
        updateStridesEarned(stats, distance, totalSeconds);

        statsRepository.save(stats);
    }

    private void updatePersonalBests(Stats stats, Double distance, String pace) {
        Integer paceSeconds = parsePaceToSeconds(pace);

        if (paceSeconds == Integer.MAX_VALUE) return; // Invalid pace


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


    public List<Run> getVisibleRuns(User currentUser) {
        if (currentUser == null) {
            return runRepository.findByVisibilityOrderByCreatedOnDesc(RunVisibility.PUBLIC);
        } else {
            return runRepository.findVisibleRunsForUser(currentUser);
        }
    }


    public List<Run> getUserRuns(User user) {
        return runRepository.findByUserOrderByCreatedOnDesc(user);
    }


    public Optional<Run> getRunIfVisible(UUID runId, User currentUser) {
        Optional<Run> runOpt = runRepository.findById(runId);
        if (runOpt.isEmpty()) return Optional.empty();

        Run run = runOpt.get();


        if (run.getVisibility() == RunVisibility.PUBLIC) {
            return runOpt;
        } else if (currentUser != null && run.getUser().getId().equals(currentUser.getId())) {
            return runOpt;
        }

        return Optional.empty();
    }


    @Transactional
    public boolean deleteRun(UUID runId, User user) {
        Optional<Run> runOpt = runRepository.findById(runId);
        if (runOpt.isEmpty() || !runOpt.get().getUser().getId().equals(user.getId())) {
            return false;
        }

        runRepository.delete(runOpt.get());
        log.info("User [{}] deleted run [{}]", user.getUsername(), runId);
        return true;
    }
}
