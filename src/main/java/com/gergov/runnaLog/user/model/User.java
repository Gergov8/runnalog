package com.gergov.runnaLog.user.model;

import com.gergov.runnaLog.comment.model.Comment;
import com.gergov.runnaLog.goal.model.Goal;
import com.gergov.runnaLog.like.model.Like;
import com.gergov.runnaLog.run.model.Run;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,  unique = true)
    private String username;

    private String firstName;

    private String lastName;

    private String profilePicture;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserCountry country;

    private boolean active;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Run> runs = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Goal> goals = new ArrayList<>();



}

