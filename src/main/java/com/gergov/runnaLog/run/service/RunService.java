package com.gergov.runnaLog.run.service;

import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.model.RunVisibility;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.stats.service.StatsService;
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
    private final StatsService statsService;


    @Autowired
    public RunService(RunRepository runRepository, StatsService statsService) {
        this.runRepository = runRepository;
        this.statsService = statsService;
    }

    public Run createRun(CreateRunRequest createRunRequest, User user) {

        // Смята колко секунди са изминали за бягането
        long totalSeconds;
        totalSeconds = calculateTotalSeconds(createRunRequest.duration());

        // Изчислява темпо в секунди на километър, след което го превръща в минути на км (MM:SS)
        String pace = calculatePace(totalSeconds, createRunRequest.distance());

        Run run = Run.builder()
                .distance(createRunRequest.distance())
                .duration(createRunRequest.duration())
                .pace(pace)
                .title(createRunRequest.title())
                .description(createRunRequest.description())
                .createdOn(LocalDateTime.now())
                .visibility(createRunRequest.visibility())
                .user(user)
                .build();

        Run savedRun =  runRepository.save(run);

        statsService.updateUserStatsAfterRun(user, createRunRequest.distance(), totalSeconds, pace);

        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds =  totalSeconds % 60;

        log.info("User [%s] created a run of [%.2f] km in [%d]:[%d]:[%d]".formatted(user.getUsername(), createRunRequest.distance(),
                hours, minutes, seconds));
        return savedRun;
    }

    private Long calculateTotalSeconds(Duration duration) {
        return duration.getSeconds();
    }

    private String calculatePace(long totalSeconds, Double distance) {

        // Темпо в секунди на км
        double paceSecondsPerKm = totalSeconds / distance;

        // Форматирани в MM:SS
        long minutes = (long) (paceSecondsPerKm / 60);
        long seconds = (long) (paceSecondsPerKm % 60);

        return String.format("%d:%02d", minutes, seconds);
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
