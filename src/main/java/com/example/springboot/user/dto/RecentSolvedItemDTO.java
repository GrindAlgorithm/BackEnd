package com.example.springboot.user.dto;

import com.example.springboot.problem.dto.TierRankDTO;
import com.example.springboot.problem.entity.ProblemEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 최근 해결한 문제 — 연동 문서 §2.15 recentSolved[] (RecentSolvedItem) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecentSolvedItemDTO {
    private String problemId;
    private String displayNo;
    private String title;
    private TierRankDTO tier;
    private LocalDateTime solvedAt;

    public static RecentSolvedItemDTO of(ProblemEntity problem, LocalDateTime solvedAt) {
        return new RecentSolvedItemDTO(
                problem.getProblemId(),
                problem.getDisplayNo(),
                problem.getTitle(),
                TierRankDTO.of(problem.getTierName(), problem.getTierLevel()),
                solvedAt
        );
    }
}
