package com.gergov.runnaLog.like.service;

import com.gergov.runnaLog.like.model.Like;
import com.gergov.runnaLog.like.repository.LikeRepository;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.user.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class LikeService {


    private final LikeRepository likeRepository;
    private final RunRepository runRepository;


    public LikeService(LikeRepository likeRepository, RunRepository runRepository) {
        this.likeRepository = likeRepository;
        this.runRepository = runRepository;
    }

    public boolean likeRun(UUID runId, User user) {
        Optional<Run> runOpt = runRepository.findById(runId);
        if (runOpt.isEmpty()) {
            return false;
        }

        Run run = runOpt.get();

        if (likeRepository.existsByUserAndRun(user, run)) {
            return false; // Вече е харесано
        }

        Like like = Like.builder()
                .user(user)
                .run(run)
                .createdOn(LocalDateTime.now())
                .build();

        likeRepository.save(like);
        return true;
    }

    public boolean unlikeRun(UUID runId, User user) {
        Optional<Run> runOpt = runRepository.findById(runId);
        if (runOpt.isEmpty()) {
            return false;
        }

        Run run = runOpt.get();

        if (!likeRepository.existsByUserAndRun(user, run)) {
            return false; // Още не е харесано
        }

        likeRepository.deleteByUserAndRun(user, run);
        return true;
    }

    public int getLikesCount(UUID runId) {
        Optional<Run> runOpt = runRepository.findById(runId);
        return runOpt.map(run -> likeRepository.findByRun(run).size()).orElse(0);
    }
}
