package com.example.springboot.common.tier;

import java.util.EnumMap;
import java.util.Map;

/**
 * 난이도(티어)별 고정 점수표. 스펙 A1 확정(절댓값) — 연동 문서 §2.6.
 * 점수는 백엔드에서 단일 관리한다: 문제 엔티티에 점수를 저장하지 않고 티어로부터 계산.
 */
public final class TierScore {

    private static final Map<TierName, Map<TierLevel, Integer>> TABLE = new EnumMap<>(TierName.class);

    static {
        // 인자 순서 = 연동 문서 점수표 컬럼 순서: V, IV, III, II, I
        put(TierName.BRONZE, 5, 6, 7, 8, 10);
        put(TierName.SILVER, 12, 14, 16, 18, 22);
        put(TierName.GOLD, 25, 30, 35, 42, 50);
        put(TierName.PLATINUM, 60, 70, 85, 100, 120);
        put(TierName.DIAMOND, 150, 180, 220, 270, 330);
    }

    private TierScore() {
    }

    private static void put(TierName name, int v, int iv, int iii, int ii, int i) {
        Map<TierLevel, Integer> row = new EnumMap<>(TierLevel.class);
        row.put(TierLevel.V, v);
        row.put(TierLevel.IV, iv);
        row.put(TierLevel.III, iii);
        row.put(TierLevel.II, ii);
        row.put(TierLevel.I, i);
        TABLE.put(name, row);
    }

    /** 티어에 해당하는 고정 점수 */
    public static int of(TierName name, TierLevel level) {
        return TABLE.get(name).get(level);
    }
}
