package com.gergov.runnaLog.like.repository;

import com.gergov.runnaLog.like.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
}
