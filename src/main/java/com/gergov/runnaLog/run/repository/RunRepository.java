package com.gergov.runnaLog.run.repository;

import com.gergov.runnaLog.run.model.Run;
import com.gergov.runnaLog.run.model.RunVisibility;
import com.gergov.runnaLog.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RunRepository extends JpaRepository<Run, UUID> {

    List<Run> findByVisibilityOrderByCreatedOnDesc(RunVisibility visibility);

    @Query("SELECT r FROM Run r WHERE r.visibility = 'PUBLIC' AND r.user <> :user ORDER BY r.createdOn DESC")
    List<Run> findVisibleRunsForUser(@Param("user") User user);

    List<Run> findByUser(User user);

    @Query("SELECT r FROM Run r WHERE r.visibility = 'PUBLIC' AND r.user = :user ORDER BY r.pace DESC")
    List<Run> findPublicRunsByUserOrderByPaceDesc(@Param("user") User user);

    @Query("SELECT r.user.id AS userId, SUM(r.distance) AS totalKm " +
            "FROM Run r " +
            "WHERE r.createdOn >= :startOfDay AND r.createdOn < :endOfDay " +
            "GROUP BY r.user.id " +
            "ORDER BY totalKm DESC")
    List<Object[]> findUsersSortedByTodayKm(@Param("startOfDay") LocalDateTime startOfDay,
                                            @Param("endOfDay") LocalDateTime endOfDay);


}
