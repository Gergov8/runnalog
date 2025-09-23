package com.gergov.runnaLog.stats.model;

import com.gergov.runnaLog.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer totalRuns;

    @Column(nullable = false)
    private Double totalDistance;

    @Column(nullable = false)
    private Integer totalDuration;

    private String pb1km;

    private String pb5km;

    private String pb10km;

    private Integer strides;

    @Column(nullable = false)
    private Integer runnerLevel;

    private LocalDateTime lastActivity;
}
