package com.example.springboot.common.tier;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 티어 등급명. 영구 티어 없음(B1) — 항상 "시즌 티어".
 * JSON 직렬화 값은 소문자 (연동 문서 §1.6 TierRank.name).
 */
@Getter
@RequiredArgsConstructor
public enum TierName {
    BRONZE("bronze"),
    SILVER("silver"),
    GOLD("gold"),
    PLATINUM("platinum"),
    DIAMOND("diamond");

    @JsonValue
    private final String value;
}
