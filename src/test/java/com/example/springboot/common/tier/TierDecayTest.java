package com.example.springboot.common.tier;

import com.example.springboot.common.tier.TierDecay.TierRank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 비활동 티어 하락 규칙 — 7일마다 1단계, 골드 IV 이상만, 하한 골드 V */
class TierDecayTest {

    private static final int PLAT_II = 2430;  // 플래티넘 II 컷
    private static final int GOLD_IV = 1450;
    private static final int GOLD_V = 1300;
    private static final int SILVER_I = 1100;

    private static TierRank rank(TierName n, TierLevel l) {
        return new TierRank(n, l);
    }

    @Test
    void ordinalRoundTripsAcrossAllTiers() {
        for (int i = 0; i < 25; i++) {
            assertEquals(i, TierRank.ofOrdinal(i).ordinal());
        }
        assertEquals(0, rank(TierName.BRONZE, TierLevel.V).ordinal());
        assertEquals(24, rank(TierName.DIAMOND, TierLevel.I).ordinal());
        assertEquals(rank(TierName.GOLD, TierLevel.I), TierRank.ofOrdinal(rank(TierName.PLATINUM, TierLevel.V).ordinal() - 1));
    }

    @Test
    void noDropBeforeSevenDays() {
        assertEquals(rank(TierName.PLATINUM, TierLevel.II), TierDecay.effectiveTier(PLAT_II, 0));
        assertEquals(rank(TierName.PLATINUM, TierLevel.II), TierDecay.effectiveTier(PLAT_II, 6));
    }

    @Test
    void dropsOneLevelPerSevenDays() {
        assertEquals(rank(TierName.PLATINUM, TierLevel.III), TierDecay.effectiveTier(PLAT_II, 7));
        assertEquals(rank(TierName.PLATINUM, TierLevel.III), TierDecay.effectiveTier(PLAT_II, 13));
        assertEquals(rank(TierName.PLATINUM, TierLevel.IV), TierDecay.effectiveTier(PLAT_II, 14));
        // 플래티넘 V 아래는 골드 I
        assertEquals(rank(TierName.GOLD, TierLevel.I), TierDecay.effectiveTier(PLAT_II, 28));
    }

    @Test
    void neverBelowGoldV() {
        assertEquals(rank(TierName.GOLD, TierLevel.V), TierDecay.effectiveTier(PLAT_II, 365));
        assertEquals(rank(TierName.GOLD, TierLevel.V), TierDecay.effectiveTier(GOLD_IV, 7));
        assertEquals(rank(TierName.GOLD, TierLevel.V), TierDecay.effectiveTier(GOLD_IV, 70));
    }

    @Test
    void goldVAndBelowDoNotDecay() {
        assertEquals(rank(TierName.GOLD, TierLevel.V), TierDecay.effectiveTier(GOLD_V, 100));
        assertEquals(rank(TierName.SILVER, TierLevel.I), TierDecay.effectiveTier(SILVER_I, 100));
        assertNull(TierDecay.warning(GOLD_V, 6));
        assertNull(TierDecay.warning(SILVER_I, 6));
    }

    @Test
    void warningOnlyWithinThreeDaysOfNextDrop() {
        assertNull(TierDecay.warning(PLAT_II, 0));
        assertNull(TierDecay.warning(PLAT_II, 3)); // 4일 남음
        TierDecay.Warning w = TierDecay.warning(PLAT_II, 5);
        assertNotNull(w);
        assertEquals(5, w.inactiveDays());
        assertEquals(2, w.daysUntilDrop());
        assertEquals(rank(TierName.PLATINUM, TierLevel.II), w.fromTier());
        assertEquals(rank(TierName.PLATINUM, TierLevel.III), w.toTier());
        // 이미 한 번 떨어진 뒤의 다음 하락 예고
        TierDecay.Warning w2 = TierDecay.warning(PLAT_II, 13);
        assertEquals(1, w2.daysUntilDrop());
        assertEquals(rank(TierName.PLATINUM, TierLevel.III), w2.fromTier());
        assertEquals(rank(TierName.PLATINUM, TierLevel.IV), w2.toTier());
    }

    @Test
    void noWarningOnceAtFloor() {
        assertNotNull(TierDecay.warning(GOLD_IV, 5));   // 골드 IV → 골드 V 예고
        assertNull(TierDecay.warning(GOLD_IV, 12));     // 이미 골드 V
    }

    @Test
    void goldGateForTitles() {
        assertTrue(TierDecay.isGoldOrAbove(GOLD_V));
        assertFalse(TierDecay.isGoldOrAbove(GOLD_V - 1));
    }
}
