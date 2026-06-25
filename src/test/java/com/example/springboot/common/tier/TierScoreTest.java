package com.example.springboot.common.tier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 고정 점수표 검증 (Spring 컨텍스트/DB 불필요) — 연동 문서 §2.6 표와 일치하는지 확인 */
class TierScoreTest {

    @Test
    void matchesContractScoreTable() {
        assertEquals(5, TierScore.of(TierName.BRONZE, TierLevel.V));
        assertEquals(10, TierScore.of(TierName.BRONZE, TierLevel.I));
        assertEquals(30, TierScore.of(TierName.GOLD, TierLevel.IV));
        assertEquals(100, TierScore.of(TierName.PLATINUM, TierLevel.II));
        assertEquals(330, TierScore.of(TierName.DIAMOND, TierLevel.I));
        assertEquals(150, TierScore.of(TierName.DIAMOND, TierLevel.V));
    }
}
