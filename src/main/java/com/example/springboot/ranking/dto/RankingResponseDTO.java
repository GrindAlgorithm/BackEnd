package com.example.springboot.ranking.dto;

import com.example.springboot.season.dto.SeasonResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** 연동 문서 §2.13 GET /rankings 응답 (RankingResponse) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponseDTO {
    private SeasonResponseDTO season;
    private List<RankingEntryResponseDTO> entries;
    private RankingEntryResponseDTO myEntry; // 목록에 없어도 채움, 비로그인이면 null

    public static RankingResponseDTO of(RankingDTO ranking) {
        List<RankingEntryResponseDTO> entries = ranking.getEntries().stream()
                .map(RankingEntryResponseDTO::of)
                .toList();
        RankingEntryResponseDTO myEntry = ranking.getMyEntry() == null
                ? null
                : RankingEntryResponseDTO.of(ranking.getMyEntry());
        SeasonResponseDTO season = ranking.getSeason() == null
                ? null
                : SeasonResponseDTO.of(ranking.getSeason());
        return new RankingResponseDTO(season, entries, myEntry);
    }
}
