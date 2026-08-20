package com.example.springboot.season.dto;

import com.example.springboot.problem.dto.ProblemSummaryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** GET /seasons/current 응답 (프론트 SeasonDetailResponse) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonDetailResponseDTO {
    private SeasonResponseDTO season;
    private double progressRatio;
    private List<ProblemSummaryResponseDTO> problems;
    private List<SeasonRewardResponseDTO> rewards;
    private List<PastSeasonResponseDTO> pastSeasons;

    public static SeasonDetailResponseDTO of(SeasonDetailDTO detail) {
        return new SeasonDetailResponseDTO(
                SeasonResponseDTO.of(detail.getSeason()),
                detail.getProgressRatio(),
                detail.getProblems().stream().map(ProblemSummaryResponseDTO::of).toList(),
                detail.getRewards().stream().map(SeasonRewardResponseDTO::of).toList(),
                detail.getPastSeasons().stream().map(PastSeasonResponseDTO::of).toList()
        );
    }
}
