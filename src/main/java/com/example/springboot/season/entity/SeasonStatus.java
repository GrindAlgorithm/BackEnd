package com.example.springboot.season.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시즌 상태. S0=베타 / 과거시즌(연습용 영구 보관) / 현재시즌(랭킹 반영) — 연동 문서 §2.5.
 * DB에는 enum 이름(CURRENT/PAST/BETA)으로 저장되고, JSON에는 소문자로 노출된다.
 */
@Getter
@RequiredArgsConstructor
public enum SeasonStatus {
    CURRENT("current"),
    PAST("past"),
    BETA("beta");

    @JsonValue
    private final String value;
}
