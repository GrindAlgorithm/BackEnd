package com.example.springboot.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/** 연동 문서 §2.7 GET /problems/{problemId} 응답 (ProblemDetail). ⚠ 본문·예제 미포함(B2). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetailResponseDTO {
    private String problemId;
    private String displayNo;
    private String title;
    private TierRankDTO tier;
    private List<String> tags;
    private String expectedComplexity;
    private int timeLimitSec;
    private int memoryLimitMb;
    private ProblemStatsDTO stats;
    private Integer points;     // 비시즌 문제는 null
    private Integer seasonId;   // 비시즌 문제는 null
    private int discussionCount;
    private ProblemMyDTO my;

    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    public static ProblemDetailResponseDTO of(ProblemDetailDTO problem) {
        ProblemStatsDTO stats = new ProblemStatsDTO(
                problem.getSubmissionCount(),
                problem.getAcceptedCount(),
                problem.getSolverCount(),
                problem.getAcceptanceRate()
        );
        String lastTriedAt = problem.getLastTriedAt() == null
                ? null
                : problem.getLastTriedAt().atOffset(KST).toString();
        ProblemMyDTO my = new ProblemMyDTO(problem.getMyStatus().getValue(), problem.getAttemptCount(), lastTriedAt);

        return new ProblemDetailResponseDTO(
                problem.getProblemId(),
                problem.getDisplayNo(),
                problem.getTitle(),
                TierRankDTO.of(problem.getTierName(), problem.getTierLevel()),
                new ArrayList<>(problem.getTags()),
                problem.getExpectedComplexity(),
                problem.getTimeLimitSec(),
                problem.getMemoryLimitMb(),
                stats,
                problem.getPoints(),
                problem.getSeasonId(),
                problem.getDiscussionCount(),
                my
        );
    }
}
