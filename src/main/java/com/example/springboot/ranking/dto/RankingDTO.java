package com.example.springboot.ranking.dto;

import com.example.springboot.season.dto.SeasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** 랭킹 서비스 모델 — 연동 문서 §2.13. 표현 변환은 {@link RankingResponseDTO}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankingDTO {
    private SeasonDTO season;
    private List<RankingEntryDTO> entries;
    private RankingEntryDTO myEntry; // 목록에 없어도 채움, 비로그인이면 null
}
