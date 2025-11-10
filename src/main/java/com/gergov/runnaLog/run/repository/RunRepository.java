package com.gergov.runnaLog.run.repository;

import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.model.RunVisibility;
import com.gergov.runnaLog.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RunRepository extends JpaRepository<Run, UUID> {

    List<Run> findByUserOrderByCreatedOnDesc(User user);

    List<Run> findByVisibilityOrderByCreatedOnDesc(RunVisibility visibility);

    @Query("SELECT r FROM Run r WHERE r.visibility = 'PUBLIC' AND r.user <> :user ORDER BY RAND()")
    List<Run> findVisibleRunsForUser(@Param("user") User user);

    List<Run> findByUser(User user);
}
