package com.example.springboot.ranking.dto;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 랭킹 항목 서비스 모델 (연동 문서 §2.13). 순위는 조회 시점에 부여. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankingEntryDTO {
    private int rank;
    private String handle;
    private TierName tierName;
    private TierLevel tierLevel;
    private int score;
    private int solvedCount;
    private LocalDateTime lastActiveAt;

    /** 엔티티 → 서비스 DTO (rank는 이후 순서대로 부여) */
    public static RankingEntryDTO of(SeasonRankingEntity e) {
        return new RankingEntryDTO(0, e.getHandle(), e.getTierName(), e.getTierLevel(),
                e.getScore(), e.getSolvedCount(), e.getLastActiveAt());
    }
}
