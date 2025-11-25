package com.gergov.runnaLog.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    private UUID id;
    private UUID userId;
    private Double distanceKm;
    private Integer daysPerWeek;
    private String planLevel;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private String planJson;
}