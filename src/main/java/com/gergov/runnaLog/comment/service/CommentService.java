package com.gergov.runnaLog.comment.service;

import com.gergov.runnaLog.comment.model.Comment;
import com.gergov.runnaLog.comment.repository.CommentRepository;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.repository.RunRepository;
import com.gergov.runnaLog.user.model.User;
import com.gergov.runnaLog.web.dto.AddCommentRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final RunRepository runRepository;


    public CommentService(CommentRepository commentRepository, RunRepository runRepository) {
        this.commentRepository = commentRepository;
        this.runRepository = runRepository;
    }

    @Transactional
    @CacheEvict(value = {"allCommentsForRun", "commentsById"}, allEntries = true)
    public void addComment(UUID runId, AddCommentRequest addCommentRequest , User user) {
        Optional<Run> runOpt = runRepository.findById(runId);
        if(runOpt.isEmpty()) {
            return;
        }

        Comment comment = Comment.builder()
                .content(addCommentRequest.getContent())
                .createdOn(LocalDateTime.now())
                .user(user)
                .run(runOpt.get())
                .build();

        commentRepository.save(comment);
    }

    @Cacheable("allCommentsForRun")
    public List<Comment> getCommentsForRun(UUID runId) {
        Optional<Run> runOpt = runRepository.findById(runId);
        return runOpt.map(commentRepository::findByRunOrderByCreatedOnAsc).orElse(null);
    }

    @Transactional
    @CacheEvict(value = {"allCommentsForRun", "commentsById"}, allEntries = true)
    public void deleteComment(User user, Run run, Comment comment) {

        boolean isCommentOwner = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().getDisplayName().equals("Admin");
        boolean isRunOwner = run.getUser().getId().equals(user.getId());

        if (!isCommentOwner && !isAdmin && !isRunOwner) {
            throw new AccessDeniedException("You are not allowed to delete this comment!");
        }

        commentRepository.delete(comment);
        log.info("User [{}] deleted comment [{}] for run [{}]", user.getUsername(), comment.getId(), run.getId());
    }

    @Cacheable("commentsById")
    public Comment getCommentById(UUID runId) {
        Optional<Comment> commentOpt = commentRepository.findById(runId);
        return commentOpt.orElse(null);
    }
}
