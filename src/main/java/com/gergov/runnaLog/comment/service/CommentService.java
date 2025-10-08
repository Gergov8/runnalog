package com.gergov.runnaLog.comment.service;

import com.gergov.runnaLog.comment.model.Comment;
import com.gergov.runnaLog.comment.repository.CommentRepository;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.user.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final RunRepository runRepository;


    public CommentService(CommentRepository commentRepository, RunRepository runRepository) {
        this.commentRepository = commentRepository;
        this.runRepository = runRepository;
    }

    public boolean addComment(UUID runId, String content, User user) {
        Optional<Run> runOpt = runRepository.findById(runId);
        if(runOpt.isEmpty()) {
            return false;
        }

        Comment comment = Comment.builder()
                .content(content)
                .createdOn(LocalDateTime.now())
                .user(user)
                .run(runOpt.get())
                .build();

        commentRepository.save(comment);
        return true;
    }

    public List<Comment> getCommentsForRun(UUID runId) {
        Optional<Run> runOpt = runRepository.findById(runId);
        return runOpt.map(commentRepository::findByRunOrderByCreatedOnAsc).orElse(null);
    }
}
