package com.example.springboot.dashboard.dto;

import com.example.springboot.season.entity.SeasonEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** 시즌 정보(홈) — 연동 문서 §2.4 dashboard.season (SeasonProgress) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonProgressDTO {
    private Integer seasonId;
    private String name;
    private String startDate;
    private String endDate;

    @JsonProperty("dDay")
    private long dDay;

    private double progressRatio; // 0~1
    private int solvedCount;      // 시즌 문제 클리어 수
    private int totalCount;       // 시즌 문제 전체 수
    private String nextProblemId; // "다음 시즌 문제 풀기" 대상 (전부 클리어 시 null)

    /**
     * @param solvedCount   클리어 수 (인증/제출 연동 전에는 0)
     * @param nextProblemId 다음 풀 문제 (없으면 null)
     */
    public static SeasonProgressDTO of(SeasonEntity season, LocalDate today,
                                       int totalCount, int solvedCount, String nextProblemId) {
        double progressRatio = totalCount > 0 ? (double) solvedCount / totalCount : 0.0;
        return new SeasonProgressDTO(
                season.getId(),
                season.getName(),
                season.getStartDate().toString(),
                season.getEndDate().toString(),
                ChronoUnit.DAYS.between(today, season.getEndDate()),
                progressRatio,
                solvedCount,
                totalCount,
                nextProblemId
        );
    }
}
