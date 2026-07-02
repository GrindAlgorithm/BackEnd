package com.example.springboot.dashboard.dto;

import com.example.springboot.problem.dto.TierRankDTO;
import com.example.springboot.problem.entity.ProblemEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 오늘의 추천 — 연동 문서 §2.4 dashboard.todayPicks[] (TodayPick) */
@Getter
@AllArgsConstructor
public class TodayPickDTO {
    private String problemId;
    private String displayNo;
    private String title;
    private TierRankDTO tier;
    private String reason;     // 표시 문구 (예: "약한 분야 · DP")
    private String reasonType; // tier_up | weak_area | continue | similar_level | category_pick

    public static TodayPickDTO of(ProblemEntity problem, String reason, String reasonType) {
        return new TodayPickDTO(
                problem.getProblemId(),
                problem.getDisplayNo(),
                problem.getTitle(),
                TierRankDTO.of(problem.getTierName(), problem.getTierLevel()),
                reason,
                reasonType
        );
    }
}
