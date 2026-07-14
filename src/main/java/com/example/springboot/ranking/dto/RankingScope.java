package com.example.springboot.ranking.dto;

/** 랭킹 범위 — 연동 문서 §2.13. season(기본)/overall/friends */
public enum RankingScope {
    SEASON,
    OVERALL,
    FRIENDS;

    /** 쿼리 파라미터 문자열 → enum (알 수 없으면 season) */
    public static RankingScope fromValue(String value) {
        if (value == null) {
            return SEASON;
        }
        return switch (value.toLowerCase()) {
            case "overall" -> OVERALL;
            case "friends" -> FRIENDS;
            default -> SEASON;
        };
    }
}
