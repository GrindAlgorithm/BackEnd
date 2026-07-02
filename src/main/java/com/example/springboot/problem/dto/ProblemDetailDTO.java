package com.example.springboot.problem.dto;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.common.tier.TierScore;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.entity.ProblemStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 문제 상세 서비스 모델 (본문 제외, 연동 문서 §2.7). 표현 변환은 {@link ProblemDetailResponseDTO}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetailDTO {
    private String problemId;
    private String displayNo;
    private String title;
    private TierName tierName;
    private TierLevel tierLevel;
    private List<String> tags;         // 알고리즘 분류
    private String expectedComplexity; // 예상 시간복잡도 (없으면 null)
    private int timeLimitSec;          // 시간 제한
    private int memoryLimitMb;         // 메모리 제한
    private long submissionCount;      // 제출
    private long acceptedCount;        // 정답
    private long solverCount;          // 맞힌 사람
    private double acceptanceRate;     // 정답 비율
    private Integer points;            // 시즌 점수 (비시즌 문제는 null)
    private Integer seasonId;          // 비시즌 문제는 null
    private int discussionCount;

    // my (인증/제출 연동 전 기본값)
    private ProblemStatus myStatus;
    private int attemptCount;
    private LocalDateTime lastTriedAt;

    public static ProblemDetailDTO of(ProblemEntity problem) {
        boolean seasonal = problem.getSeason() != null;
        Integer points = seasonal ? TierScore.of(problem.getTierName(), problem.getTierLevel()) : null;
        Integer seasonId = seasonal ? problem.getSeason().getId() : null;
        return new ProblemDetailDTO(
                problem.getProblemId(),
                problem.getDisplayNo(),
                problem.getTitle(),
                problem.getTierName(),
                problem.getTierLevel(),
                new ArrayList<>(problem.getTags()),
                problem.getExpectedComplexity(),
                problem.getTimeLimitSec(),
                problem.getMemoryLimitMb(),
                problem.getSubmissionCount(),
                problem.getAcceptedCount(),
                problem.getSolverCount(),
                problem.acceptanceRate(),
                points,
                seasonId,
                problem.getDiscussionCount(),
                // 인증/제출 연동 전: 미시도 / 시도 0 / 마지막 시도 없음
                ProblemStatus.UNTRIED, 0, null
        );
    }
}
