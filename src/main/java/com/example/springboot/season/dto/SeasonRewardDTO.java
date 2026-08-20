package com.example.springboot.season.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 시즌 리워드 서비스 모델 (프론트 SeasonReward, GET /seasons/current).
 * achieved/progressText 는 조회 유저 기준 계산값 — SeasonServiceImpl.buildReward.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonRewardDTO {
    private String id;
    private String name;
    private String colorKey;
    private String condition;
    private boolean achieved;
    private String progressText; // "진행중 (7/15)" | "달성"
}
