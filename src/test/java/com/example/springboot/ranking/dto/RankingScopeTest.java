package com.example.springboot.ranking.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 랭킹 scope 파싱 검증 (Spring/DB 불필요) — 연동 문서 §2.13 */
class RankingScopeTest {

    @Test
    void parsesScopeWithSeasonDefault() {
        assertEquals(RankingScope.SEASON, RankingScope.fromValue("season"));
        assertEquals(RankingScope.OVERALL, RankingScope.fromValue("overall"));
        assertEquals(RankingScope.FRIENDS, RankingScope.fromValue("friends"));
        assertEquals(RankingScope.SEASON, RankingScope.fromValue(null));   // 기본
        assertEquals(RankingScope.SEASON, RankingScope.fromValue("bogus")); // 알 수 없으면 season
    }
}
