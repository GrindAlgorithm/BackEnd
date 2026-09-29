package com.example.springboot.problem.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.problem.entity.SolveSessionEntity;
import com.example.springboot.problem.repository.SolveSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실행·제출이 "이 유저가 이 문제 본문을 연 세션"에서 나왔는지 검증한다 (B2, 연동 문서 §2.8).
 * 세션은 POST /problems/{id}/open 에서만 발급되므로, 통과하면 열람 시각·무결성 신호(§2.17)와
 * 제출을 같은 세션으로 조인할 수 있다.
 */
@Component
@RequiredArgsConstructor
public class SolveSessionGuard {

    static final String CODE = "INVALID_SOLVE_SESSION";
    static final String MESSAGE = "풀이 세션이 유효하지 않습니다. 문제를 다시 열어 주세요";

    private final SolveSessionRepository solveSessionRepository;

    /** 세션이 없거나, 다른 문제의 세션이거나, 다른 유저의 세션이면 400 INVALID_SOLVE_SESSION. */
    @Transactional(readOnly = true)
    public SolveSessionEntity verify(String solveSessionId, String problemId, String handle) {
        if (solveSessionId == null || solveSessionId.isBlank()) {
            throw ApiException.badRequest(CODE, MESSAGE);
        }
        SolveSessionEntity session = solveSessionRepository.findById(solveSessionId)
                .orElseThrow(() -> ApiException.badRequest(CODE, MESSAGE));
        if (!session.getProblem().getProblemId().equals(problemId) || !session.getUserHandle().equals(handle)) {
            throw ApiException.badRequest(CODE, MESSAGE);
        }
        return session;
    }
}
