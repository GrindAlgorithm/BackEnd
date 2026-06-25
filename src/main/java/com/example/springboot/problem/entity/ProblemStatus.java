package com.example.springboot.problem.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 로그인 유저 기준 문제 진행 상태 — 연동 문서 §1.6 ProblemStatus.
 * (인증/제출 기능 연동 전까지는 항상 UNTRIED 로 응답)
 */
@Getter
@RequiredArgsConstructor
public enum ProblemStatus {
    CLEARED("cleared"),
    WIP("wip"),
    UNTRIED("untried");

    @JsonValue
    private final String value;
}
