package com.example.springboot.dashboard.dto;

import com.example.springboot.problem.dto.TierRankDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 내 주변 순위(±2) — 연동 문서 §2.4 dashboard.nearbyRanking[] (NearbyRankingEntry) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NearbyRankingEntryDTO {
    private int rank;
    private String handle;
    private TierRankDTO tier;
    private int weeklyDelta; // 7일 순위 변동 (+면 상승)

    @JsonProperty("isMe")
    private boolean isMe;
}
