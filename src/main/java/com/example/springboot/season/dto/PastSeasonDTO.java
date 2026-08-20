package com.example.springboot.season.dto;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 지난 시즌 요약 서비스 모델 (프론트 PastSeasonRow, GET /seasons/current).
 * 챔피언 = 해당 시즌 랭킹 1위 (season_ranking 점수 내림차순 첫 행).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PastSeasonDTO {
    private Integer id;
    private String name;
    private String periodText; // "4/1 ~ 6/30" | "베타 시즌"
    private String championHandle;
    private TierName championTierName;
    private TierLevel championTierLevel;
}
