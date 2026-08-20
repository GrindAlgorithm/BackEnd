package com.example.springboot.season.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** GET /seasons/current 응답의 rewards 항목 (프론트 SeasonReward) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonRewardResponseDTO {
    private String id;
    private String name;
    private String colorKey;
    private String condition;
    private boolean achieved;
    private String progressText;

    public static SeasonRewardResponseDTO of(SeasonRewardDTO reward) {
        return new SeasonRewardResponseDTO(
                reward.getId(),
                reward.getName(),
                reward.getColorKey(),
                reward.getCondition(),
                reward.isAchieved(),
                reward.getProgressText()
        );
    }
}
