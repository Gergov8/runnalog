package com.gergov.runnaLog.run.service;

import com.gergov.runnaLog.like.service.LikeService;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.model.RunVisibility;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.stats.service.StatsService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.repository.UserRepository;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import com.gergov.runnaLog.web.dto.RunResponseDto;
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
    private final LikeService likeService;
    private final UserRepository userRepository;


    @Autowired
    public RunService(RunRepository runRepository, StatsService statsService, LikeService likeService, UserRepository userRepository) {
        this.runRepository = runRepository;
        this.statsService = statsService;
        this.likeService = likeService;
        this.userRepository = userRepository;
    }

    public Run createRun(CreateRunRequest createRunRequest, User user) {

        Duration duration = Duration.ofHours(createRunRequest.getDurationHours())
                .plusMinutes(createRunRequest.getDurationMinutes())
                .plusSeconds(createRunRequest.getDurationSeconds());

        // Смята колко секунди са изминали за бягането
        long totalSeconds = duration.getSeconds();

        // Изчислява темпо в секунди на километър, след което го превръща в минути на км (MM:SS)
        String pace = calculatePace(totalSeconds, createRunRequest.getDistance());

        Run run = Run.builder()
                .distance(createRunRequest.getDistance())
                .duration(duration)
                .pace(pace)
                .title(createRunRequest.getTitle())
                .description(createRunRequest.getDescription())
                .createdOn(LocalDateTime.now())
                .visibility(createRunRequest.getVisibility())
                .user(user)
                .build();

        Run savedRun =  runRepository.save(run);

        statsService.updateUserStatsAfterRun(user, createRunRequest.getDistance(), totalSeconds, pace);

        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds =  totalSeconds % 60;

        log.info("User [%s] created a run of [%.2f] km in [%d]:[%d]:[%d]".formatted(user.getUsername(), createRunRequest.getDistance(),
                hours, minutes, seconds));
        return savedRun;
    }

    private Duration parseDuration(String input) {
        // очаква "HH:MM:SS" или "MM:SS"
        String[] parts = input.split(":");
        if (parts.length == 3) {
            return Duration.ofHours(Long.parseLong(parts[0]))
                    .plusMinutes(Long.parseLong(parts[1]))
                    .plusSeconds(Long.parseLong(parts[2]));
        } else if (parts.length == 2) {
            return Duration.ofMinutes(Long.parseLong(parts[0]))
                    .plusSeconds(Long.parseLong(parts[1]));
        }
        throw new IllegalArgumentException("Invalid duration format: " + input);
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

    public List<RunResponseDto> getFeed(User currentUser) {
        return getVisibleRuns(currentUser).stream()
                .map(run -> new RunResponseDto(
                        run.getId(), // this was missing
                        run.getUser().getUsername(),
                        run.getDistance(),
                        formatDuration(run.getDuration()),
                        run.getPace(),
                        run.getTitle(),
                        run.getLikes().size(), // likesCount
                        run.getLikes().contains(currentUser) // likedByCurrentUser
                ))
                .toList();
    }



//    public List<Run> getFeedWithLikes(User currentUser) {
//        List<Run> runs = getVisibleRuns(currentUser);
//
//        for (Run run : runs) {
//            int likes = likeService.getLikesCount(run.getId());
//            run.setLikesCount(likes);
//
//            User user = userRepository.findById(currentUser.getId()).orElseThrow();
//            boolean likedByUser = likeService.isRunLikedByUser(user, run);
//            run.setLikedByCurrentUser(likedByUser);
//
//            // Example scoring formula: likes + recency in hours
//            long hoursAgo = java.time.Duration.between(run.getCreatedOn(), java.time.LocalDateTime.now()).toHours();
//            run.setScore(likes * 2.0 + Math.max(0, 100 - hoursAgo));
//        }
//
//        // Sort by score descending
//        runs.sort((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()));
//        return runs;
//    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
    }


    public List<Run> getRunsByUser(User user) {
        return runRepository.findByUser(user);
    }

}
