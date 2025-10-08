package com.gergov.runnaLog.like.repository;

import com.gergov.runnaLog.like.model.Like;
import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    boolean existsByUserAndRun(User user, Run run);
    List<Like> findByRun(Run run);
    void deleteByUserAndRun(User user, Run run);
}
