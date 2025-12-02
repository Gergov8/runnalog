package com.gergov.runnaLog.run.service;

import com.gergov.runnaLog.like.service.LikeService;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.model.RunVisibility;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.stats.service.StatsService;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.user.model.UserRole;
import com.gergov.runnaLog.web.dto.CreateRunRequest;
import com.gergov.runnaLog.web.dto.RunResponseDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RunService {

    private final RunRepository runRepository;
    private final StatsService statsService;
    private final LikeService likeService;

    @Autowired
    public RunService(RunRepository runRepository, StatsService statsService, LikeService likeService) {
        this.runRepository = runRepository;
        this.statsService = statsService;
        this.likeService = likeService;
    }

    @Transactional
    @CacheEvict(value = {"visibleRuns", "feed"}, allEntries = true)
    public void createRun(CreateRunRequest createRunRequest, User user) {

        Duration duration = Duration.ofHours(createRunRequest.getDurationHours())
                .plusMinutes(createRunRequest.getDurationMinutes())
                .plusSeconds(createRunRequest.getDurationSeconds());

        long totalSeconds = duration.getSeconds();
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

        runRepository.save(run);
        statsService.updateUserStatsAfterRun(user, createRunRequest.getDistance(), totalSeconds, pace);

        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds = totalSeconds % 60;

        log.info("User [%s] created a run of [%.2f] km in [%d]:[%d]:[%d]".formatted(
                user.getUsername(),
                createRunRequest.getDistance(),
                hours,
                minutes,
                seconds));
    }

    private String calculatePace(long totalSeconds, Double distance) {

        double paceSecondsPerKm = totalSeconds / distance;
        long minutes = (long) (paceSecondsPerKm / 60);
        long seconds = (long) (paceSecondsPerKm % 60);

        return String.format("%d:%02d", minutes, seconds);
    }

    @Cacheable(value = "visibleRuns", key = "#currentUser != null ? #currentUser.id : 'guest'")
    public List<Run> getVisibleRuns(User currentUser) {

        if (currentUser == null) {

            return runRepository.findByVisibilityOrderByCreatedOnDesc(RunVisibility.PUBLIC);
        } else {

            return runRepository.findVisibleRunsForUser(currentUser);
        }
    }

    @Transactional
    @CacheEvict(value = {"visibleRuns", "feed"}, allEntries = true)
    public void deleteRun(User user, UUID runId) {

        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("Run not found"));

        boolean isOwner = user.getId().equals(run.getUser().getId());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to delete this run!");
        }

        runRepository.delete(run);
        statsService.updateUserStatsAfterDeleteRun(user, run.getDistance(), run.getDuration().getSeconds());
        log.info("User [{}] deleted run [{}]", user.getUsername(), run.getId());
    }

    @Cacheable(value = "feed", key = "#currentUser != null ? #currentUser.id : 'guest'")
    public List<RunResponseDto> getFeed(User currentUser) {

        return getVisibleRuns(currentUser).stream()
                .map(run -> new RunResponseDto(
                        run.getId(),
                        run.getUser().getId(),
                        run.getUser().getUsername(),
                        run.getUser().getProfilePicture(),
                        run.getDistance(),
                        formatDuration(run.getDuration()),
                        run.getPace(),
                        run.getTitle(),
                        likeService.getLikesCount(run.getId()),
                        likeService.isRunLikedByUser(currentUser, run)
                ))
                .toList();
    }

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

    public Run getRunById(UUID runId) {

        return runRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Run with [%s] id not found.".formatted(runId)));
    }
}
