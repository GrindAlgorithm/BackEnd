package com.example.springboot.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 프로필 통계 — 연동 문서 §2.15 stats (UserStats) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private int solvedCount;
    private int submissionCount;
    private double accuracyRate;      // % (0~100, 소수 첫째 자리)
    private double avgAttempts;       // 해결 문제당 평균 제출 수 (소수 첫째 자리)
    private int streakDays;
    private int longestStreakDays;
}
