package com.example.springboot.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 연동 문서 §2.6 GET /seasons/{id}/problems 응답 항목 (ProblemSummary) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSummaryResponseDTO {
    private String problemId;
    private String displayNo;
    private String title;
    private List<String> tags;
    private TierRankDTO tier;
    private double acceptanceRate; // % (0~100, 소수 첫째 자리)
    private int points;            // 난이도별 고정 점수 (A1 확정)
    private String myStatus;       // cleared | wip | untried

    public static ProblemSummaryResponseDTO of(ProblemSummaryDTO problem) {
        return new ProblemSummaryResponseDTO(
                problem.getProblemId(),
                problem.getDisplayNo(),
                problem.getTitle(),
                new ArrayList<>(problem.getTags()),
                TierRankDTO.of(problem.getTierName(), problem.getTierLevel()),
                problem.getAcceptanceRate(),
                problem.getPoints(),
                problem.getMyStatus().getValue()
        );
    }
}
