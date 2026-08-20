package com.example.springboot.season.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시즌 리워드(챌린지) 정의 — 시즌 화면 리워드 목록 (요건 5, GET /seasons/current).
 * 달성 여부는 저장하지 않는다 — 조회 시 유저의 랭킹/클리어 이력으로 계산.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "season_reward")
public class SeasonRewardEntity {

    /** 프론트 SeasonReward.id (예: "s2_champion") */
    @Id
    @Column(length = 32)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private SeasonEntity season;

    @Column(nullable = false, length = 64)
    private String name;

    /** bronze..diamond | green | blue — 프론트 TitleColorKey */
    @Column(name = "color_key", nullable = false, length = 16)
    private String colorKey;

    /** 화면 노출용 조건 설명 */
    @Column(name = "condition_text", nullable = false)
    private String conditionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 24)
    private RewardConditionType conditionType;

    /** CLEAR_COUNT/SOLVE_COUNT 기준값, 그 외 null */
    @Column
    private Integer threshold;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public static SeasonRewardEntity createSeasonRewardEntity(String id, SeasonEntity season, String name,
                                                              String colorKey, String conditionText,
                                                              RewardConditionType conditionType,
                                                              Integer threshold, int sortOrder) {
        return new SeasonRewardEntity(id, season, name, colorKey, conditionText, conditionType, threshold, sortOrder);
    }
}
