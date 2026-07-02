package com.example.springboot.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §2.7 problem.stats = { 제출/정답/맞힌 사람/정답 비율 } */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemStatsDTO {
    private long submissionCount; // 제출
    private long acceptedCount;   // 정답
    private long solverCount;     // 맞힌 사람
    private double acceptanceRate; // 정답 비율 (% 0~100)
}
