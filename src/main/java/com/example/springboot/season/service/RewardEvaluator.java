package com.example.springboot.season.service;

import com.example.springboot.common.tier.TierCut;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.season.entity.SeasonRewardEntity;

/**
 * 시즌 리워드 달성 판정 — 시즌 화면 진행 표시(GET /seasons/current)와 칭호 발급(TitleService)이 함께 쓴다.
 * 티어 조건은 저장된 티어(비활동 하락 반영)가 아니라 점수 기준 티어로 본다 — "도달"은 한 번 닿으면 달성.
 */
public final class RewardEvaluator {

    /** 판정 입력 — 한 유저의 한 시즌 기록. 미배치면 rank/score 가 null */
    public record Progress(Integer rank, Integer score, int solvedCount, int clearedCount, int totalCount,
                           boolean seasonEnded) {
    }

    public record Result(boolean achieved, String progressText) {
    }

    private RewardEvaluator() {
    }

    public static Result evaluate(SeasonRewardEntity reward, Progress p) {
        return switch (reward.getConditionType()) {
            case CHAMPION -> {
                // 시즌 종료 시점에 확정 — 진행 중에는 현재 순위만 노출
                boolean achieved = p.seasonEnded() && p.rank() != null && p.rank() == 1;
                yield new Result(achieved, achieved ? "달성"
                        : p.rank() == null ? "진행중 (순위 없음)" : "진행중 (" + p.rank() + "위)");
            }
            case REACH_DIAMOND -> {
                if (p.score() == null) {
                    yield new Result(false, "진행중 (미배치)");
                }
                TierName tier = TierCut.nameOf(p.score());
                boolean achieved = tier == TierName.DIAMOND;
                yield new Result(achieved, achieved ? "달성"
                        : "진행중 (" + koreanTier(tier) + " " + TierCut.levelOf(p.score()).name() + ")");
            }
            case CLEAR_ALL -> {
                boolean achieved = p.totalCount() > 0 && p.clearedCount() >= p.totalCount();
                yield new Result(achieved, achieved ? "달성" : "진행중 (" + p.clearedCount() + "/" + p.totalCount() + ")");
            }
            case CLEAR_COUNT -> {
                int need = reward.getThreshold() == null ? 1 : reward.getThreshold();
                boolean achieved = p.clearedCount() >= need;
                yield new Result(achieved, achieved ? "달성" : "진행중 (" + p.clearedCount() + "/" + need + ")");
            }
            case SOLVE_COUNT -> {
                int need = reward.getThreshold() == null ? 0 : reward.getThreshold();
                boolean achieved = need > 0 && p.solvedCount() >= need;
                yield new Result(achieved, achieved ? "달성" : "진행중 (" + p.solvedCount() + "/" + need + ")");
            }
        };
    }

    /** 티어 한글 표기 (리워드 진행 문구용) */
    static String koreanTier(TierName tierName) {
        return switch (tierName) {
            case BRONZE -> "브론즈";
            case SILVER -> "실버";
            case GOLD -> "골드";
            case PLATINUM -> "플래티넘";
            case DIAMOND -> "다이아";
        };
    }
}
