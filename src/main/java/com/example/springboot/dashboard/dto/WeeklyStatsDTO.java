package com.example.springboot.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 이번주 통계 — 연동 문서 §2.4 dashboard.weekly (WeeklyStats) */
@Getter
@AllArgsConstructor
public class WeeklyStatsDTO {
    private int solvedCount;
    private int scoreGained;
    private int streakDays;
    private double accuracyRate; // % (0~100)

    /** 제출/활동 도메인 연동 전 기본값 (모두 0) */
    public static WeeklyStatsDTO empty() {
        return new WeeklyStatsDTO(0, 0, 0, 0.0);
    }
}
