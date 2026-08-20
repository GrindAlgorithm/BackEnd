package com.example.springboot.season.dto;

import com.example.springboot.problem.dto.ProblemSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 시즌 화면 통합 서비스 모델 (프론트 SeasonDetailResponse, GET /seasons/current).
 * ⚠ progressRatio 는 시즌 "기간 경과" 비율(0~1) — 대시보드의 클리어 비율과 다르다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonDetailDTO {
    private SeasonDTO season;
    private double progressRatio;
    private List<ProblemSummaryDTO> problems;
    private List<SeasonRewardDTO> rewards;
    private List<PastSeasonDTO> pastSeasons;
}
