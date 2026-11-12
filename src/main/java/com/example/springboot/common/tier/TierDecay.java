package com.example.springboot.common.tier;

/**
 * 비활동 티어 하락 규칙 (A1 하락 공식 — 2026-11-12 확정).
 * <ul>
 *   <li>마지막 정답(아무 문제나) 이후 {@value #PERIOD_DAYS}일마다 티어가 한 단계 내려간다.</li>
 *   <li>점수·순위는 그대로 — 티어 표시만 내려가고, 다음 정답 때 점수 기준 티어로 즉시 복구된다.</li>
 *   <li>골드 IV 이상만 하락하며 골드 V 아래로는 내려가지 않는다.</li>
 * </ul>
 * 입력(점수, 비활동 일수)만으로 결과가 정해지는 순수 함수라 스케줄러를 몇 번 돌려도 결과가 같다.
 */
public final class TierDecay {

    /** 하락 주기(일) — 비활동 7일째에 첫 하락 */
    public static final int PERIOD_DAYS = 7;
    /** 하락 예고 — 다음 하락까지 이 일수 이하로 남으면 경고 배너를 띄운다 */
    public static final int WARN_DAYS = 3;
    /** 하한 티어 — 이 티어 이하로는 하락하지 않는다 */
    private static final TierRank FLOOR = new TierRank(TierName.GOLD, TierLevel.V);

    /** 티어 한 칸 (이름 + 레벨) */
    public record TierRank(TierName name, TierLevel level) {

        /** 전체 서열 — 브론즈 V = 0 … 다이아 I = 24 */
        public int ordinal() {
            // TierLevel 은 I..V 순서라 V 가 가장 낮다
            return name.ordinal() * 5 + (TierLevel.values().length - 1 - level.ordinal());
        }

        public static TierRank ofOrdinal(int ordinal) {
            TierName name = TierName.values()[ordinal / 5];
            TierLevel level = TierLevel.values()[TierLevel.values().length - 1 - ordinal % 5];
            return new TierRank(name, level);
        }

        public static TierRank ofScore(int score) {
            return new TierRank(TierCut.nameOf(score), TierCut.levelOf(score));
        }
    }

    /** 하락 경고 — 다음 하락까지 남은 일수와 전후 티어 */
    public record Warning(int inactiveDays, int daysUntilDrop, TierRank fromTier, TierRank toTier) {
    }

    private TierDecay() {
    }

    /** 비활동 일수만큼 하락한 티어. 하락 대상이 아니면 점수 기준 티어 그대로 */
    public static TierRank effectiveTier(int score, int inactiveDays) {
        TierRank base = TierRank.ofScore(score);
        int drops = Math.max(0, inactiveDays) / PERIOD_DAYS;
        if (drops == 0 || base.ordinal() <= FLOOR.ordinal()) {
            return base;
        }
        return TierRank.ofOrdinal(Math.max(FLOOR.ordinal(), base.ordinal() - drops));
    }

    /**
     * 곧 하락할 예정이면 경고, 아니면 null.
     * 이미 하한에 닿았거나(더 내려갈 곳 없음) 다음 하락까지 {@value #WARN_DAYS}일보다 많이 남았으면 null.
     */
    public static Warning warning(int score, int inactiveDays) {
        if (inactiveDays <= 0) {
            return null;
        }
        TierRank now = effectiveTier(score, inactiveDays);
        if (now.ordinal() <= FLOOR.ordinal()) {
            return null;
        }
        int daysUntilDrop = PERIOD_DAYS - inactiveDays % PERIOD_DAYS;
        if (daysUntilDrop > WARN_DAYS) {
            return null;
        }
        return new Warning(inactiveDays, daysUntilDrop, now, TierRank.ofOrdinal(now.ordinal() - 1));
    }

    /** 칭호 발급 하한(A4 "골드 이상부터") 판정용 — 점수 기준 티어가 골드 이상인지 */
    public static boolean isGoldOrAbove(int score) {
        return TierCut.nameOf(score).ordinal() >= TierName.GOLD.ordinal();
    }
}
