package com.gergov.runnaLog.feign.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeignPlanRequest {
        private UUID userId;
        private Double distanceKm;
        private String planLevel;
        private int daysPerWeek;
 }