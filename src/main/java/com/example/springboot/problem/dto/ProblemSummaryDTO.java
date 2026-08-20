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

import java.util.ArrayList;
import java.util.List;

/** 시즌 문제 서비스 모델 (연동 문서 §2.6). 표현 변환은 {@link ProblemSummaryResponseDTO} 에서 수행. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSummaryDTO {
    private String problemId;
    private String displayNo;
    private String title;
    private List<String> tags;
    private TierName tierName;
    private TierLevel tierLevel;
    private double acceptanceRate; // % (0~100, 소수 첫째 자리)
    private int points;            // 난이도별 고정 점수 (A1 확정)
    private ProblemStatus myStatus;

    /** 미로그인/유저 이력 없는 문맥 — 전부 미시도 (연동 문서 §2.6) */
    public static ProblemSummaryDTO of(ProblemEntity problem) {
        return of(problem, ProblemStatus.UNTRIED);
    }

    /** 로그인 유저의 제출 이력으로 계산한 myStatus 를 함께 싣는 문맥 (시즌 화면 등) */
    public static ProblemSummaryDTO of(ProblemEntity problem, ProblemStatus myStatus) {
        return new ProblemSummaryDTO(
                problem.getProblemId(),
                problem.getDisplayNo(),
                problem.getTitle(),
                new ArrayList<>(problem.getTags()),
                problem.getTierName(),
                problem.getTierLevel(),
                problem.acceptanceRate(),
                TierScore.of(problem.getTierName(), problem.getTierLevel()),
                myStatus
        );
    }
}
