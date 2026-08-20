package com.example.springboot.season.entity;

/**
 * 시즌 리워드 달성 조건 유형. 달성 여부/진행 문구는 저장하지 않고
 * 조회 시 유저별로 계산한다 — SeasonServiceImpl.buildReward 참고.
 */
public enum RewardConditionType {
    /** 시즌 종료 시 1위 — 진행 중에는 항상 미달성, 현재 순위만 노출 */
    CHAMPION,
    /** 시즌 다이아 티어 도달 */
    REACH_DIAMOND,
    /** 시즌 문제 전부 클리어 */
    CLEAR_ALL,
    /** 시즌 문제 threshold개 클리어 */
    CLEAR_COUNT,
    /** 시즌 중 threshold문제 풀이 (시즌 랭킹 solved_count 기준) */
    SOLVE_COUNT
}
