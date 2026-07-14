package com.example.springboot.ranking.dto;

import com.example.springboot.problem.dto.TierRankDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneOffset;

/** 연동 문서 §2.13 랭킹 항목 (RankingEntry) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankingEntryResponseDTO {
    private int rank;
    private String handle;
    private TierRankDTO tier;
    private int score;
    private int solvedCount;
    private String lastActiveAt; // ISO 8601 (+09:00)

    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    public static RankingEntryResponseDTO of(RankingEntryDTO e) {
        return new RankingEntryResponseDTO(
                e.getRank(),
                e.getHandle(),
                TierRankDTO.of(e.getTierName(), e.getTierLevel()),
                e.getScore(),
                e.getSolvedCount(),
                e.getLastActiveAt().atOffset(KST).toString()
        );
    }
}
