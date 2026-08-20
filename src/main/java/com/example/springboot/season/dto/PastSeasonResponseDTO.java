package com.example.springboot.season.dto;

import com.example.springboot.problem.dto.TierRankDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** GET /seasons/current 응답의 pastSeasons 항목 (프론트 PastSeasonRow) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PastSeasonResponseDTO {
    private Integer id;
    private String name;
    private String periodText;
    private ChampionDTO champion;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChampionDTO {
        private String handle;
        private TierRankDTO tier;
    }

    public static PastSeasonResponseDTO of(PastSeasonDTO past) {
        return new PastSeasonResponseDTO(
                past.getId(),
                past.getName(),
                past.getPeriodText(),
                new ChampionDTO(
                        past.getChampionHandle(),
                        TierRankDTO.of(past.getChampionTierName(), past.getChampionTierLevel())
                )
        );
    }
}
