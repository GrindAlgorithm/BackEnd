package com.example.springboot.season.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시즌 상태. S0=베타 / 과거시즌(연습용 영구 보관) / 현재시즌(랭킹 반영) / 공개 전 시즌 — 연동 문서 §2.5.
 * DB에는 enum 이름(CURRENT/PAST/BETA/UPCOMING)으로 저장되고, JSON에는 소문자로 노출된다.
 * UPCOMING 은 시작일 전에 미리 넣어 둔 시즌 — 공개 API 에서 시즌·문제 모두 숨긴다(스포일러·선행 풀이 방지).
 * 날짜 기준 전환은 SeasonLifecycleService 가 한다.
 */
@Getter
@RequiredArgsConstructor
public enum SeasonStatus {
    CURRENT("current"),
    PAST("past"),
    BETA("beta"),
    UPCOMING("upcoming");

    /** 공개 API 에 노출되는 시즌인가 */
    public boolean isPublic() {
        return this != UPCOMING;
    }

    @JsonValue
    private final String value;
}
