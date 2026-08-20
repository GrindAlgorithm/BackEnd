package com.example.springboot.common.tier;

/**
 * 누적 시즌 점수 → 유저 티어 배치 컷 (요건 9·채점→랭킹 연동).
 * ⚠ 컷 값은 시드 랭킹(ranking-seed.sql)과 모순 없게 잠정 캘리브레이션한 것 —
 * 운영 밸런싱(A1 후속) 시 이 표만 바꾸면 된다. 티어는 정답 반영 시점에만 재계산된다.
 */
public final class TierCut {

    /** (누적 점수 하한, 티어) — 내림차순으로 첫 매치. */
    private record Cut(int minScore, TierName name, TierLevel level) {
    }

    private static final Cut[] CUTS = {
            new Cut(3200, TierName.DIAMOND, TierLevel.I),
            new Cut(3050, TierName.DIAMOND, TierLevel.II),
            new Cut(2900, TierName.DIAMOND, TierLevel.III),
            new Cut(2750, TierName.DIAMOND, TierLevel.IV),
            new Cut(2600, TierName.DIAMOND, TierLevel.V),
            new Cut(2500, TierName.PLATINUM, TierLevel.I),
            new Cut(2430, TierName.PLATINUM, TierLevel.II),
            new Cut(2350, TierName.PLATINUM, TierLevel.III),
            new Cut(2200, TierName.PLATINUM, TierLevel.IV),
            new Cut(2050, TierName.PLATINUM, TierLevel.V),
            new Cut(1900, TierName.GOLD, TierLevel.I),
            new Cut(1750, TierName.GOLD, TierLevel.II),
            new Cut(1600, TierName.GOLD, TierLevel.III),
            new Cut(1450, TierName.GOLD, TierLevel.IV),
            new Cut(1300, TierName.GOLD, TierLevel.V),
            new Cut(1100, TierName.SILVER, TierLevel.I),
            new Cut(950, TierName.SILVER, TierLevel.II),
            new Cut(800, TierName.SILVER, TierLevel.III),
            new Cut(650, TierName.SILVER, TierLevel.IV),
            new Cut(500, TierName.SILVER, TierLevel.V),
            new Cut(400, TierName.BRONZE, TierLevel.I),
            new Cut(300, TierName.BRONZE, TierLevel.II),
            new Cut(200, TierName.BRONZE, TierLevel.III),
            new Cut(100, TierName.BRONZE, TierLevel.IV),
            new Cut(0, TierName.BRONZE, TierLevel.V),
    };

    private TierCut() {
    }

    /** 누적 점수에 해당하는 티어명. */
    public static TierName nameOf(int score) {
        return cutOf(score).name();
    }

    /** 누적 점수에 해당하는 티어 레벨. */
    public static TierLevel levelOf(int score) {
        return cutOf(score).level();
    }

    private static Cut cutOf(int score) {
        for (Cut cut : CUTS) {
            if (score >= cut.minScore()) {
                return cut;
            }
        }
        return CUTS[CUTS.length - 1];
    }
}
