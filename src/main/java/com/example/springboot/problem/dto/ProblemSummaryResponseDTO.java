package com.example.springboot.problem.dto;

import com.example.springboot.common.tier.TierScore;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.entity.ProblemStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/** 연동 문서 §2.6 GET /seasons/{id}/problems 응답 항목 (ProblemSummary) */
@Getter
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

    public static ProblemSummaryResponseDTO of(ProblemEntity problem) {
        return new ProblemSummaryResponseDTO(
                problem.getProblemId(),
                problem.getDisplayNo(),
                problem.getTitle(),
                new ArrayList<>(problem.getTags()),
                TierRankDTO.of(problem.getTierName(), problem.getTierLevel()),
                acceptanceRate(problem.getSubmissionCount(), problem.getAcceptedCount()),
                TierScore.of(problem.getTierName(), problem.getTierLevel()),
                // 인증/제출 연동 전: 항상 미시도 (연동 문서 §2.6 — 미로그인 시 전부 untried)
                ProblemStatus.UNTRIED.getValue()
        );
    }

    private static double acceptanceRate(long submissionCount, long acceptedCount) {
        if (submissionCount <= 0) {
            return 0.0;
        }
        return Math.round(acceptedCount * 1000.0 / submissionCount) / 10.0;
    }
}
