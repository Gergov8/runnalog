package com.gergov.runnaLog.comment.repository;

import com.gergov.runnaLog.comment.model.Comment;
import com.gergov.runnaLog.run.model.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByRunOrderByCreatedOnAsc(Run run);
}
