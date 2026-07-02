package com.example.springboot.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §2.7 problem.my = 로그인 유저의 진행 상태 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemMyDTO {
    private String status;      // cleared | wip | untried
    private int attemptCount;
    private String lastTriedAt; // ISO 8601 (+09:00) 또는 null
}
