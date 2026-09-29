package com.example.springboot.problem.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.entity.SolveSessionEntity;
import com.example.springboot.problem.repository.SolveSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** 실행·제출의 풀이 세션 검증 (B2) — 세션 없음/빈 값/다른 문제/다른 유저는 전부 거부 */
@ExtendWith(MockitoExtension.class)
class SolveSessionGuardTest {

    @Mock private SolveSessionRepository solveSessionRepository;

    @InjectMocks private SolveSessionGuard guard;

    private static final ProblemEntity PROBLEM = ProblemEntity.createProblemEntity("conquest", "A", "정복",
            TierName.GOLD, TierLevel.IV, null, List.of(), 2, 256, null, 0, 0, 0, 0);

    private void givenSession(String id, String handle) {
        when(solveSessionRepository.findById(id)).thenReturn(Optional.of(
                SolveSessionEntity.createSolveSessionEntity(id, PROBLEM, handle, LocalDateTime.now())));
    }

    private void assertRejected(Runnable call) {
        ApiException ex = assertThrows(ApiException.class, call::run);
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_SOLVE_SESSION", ex.getCode());
    }

    @Test
    void ownSessionForSameProblemPasses() {
        givenSession("s1", "park");

        assertEquals("s1", guard.verify("s1", "conquest", "park").getId());
    }

    @Test
    void nullOrBlankSessionIsRejected() {
        assertRejected(() -> guard.verify(null, "conquest", "park"));
        assertRejected(() -> guard.verify("  ", "conquest", "park"));
    }

    @Test
    void unknownSessionIsRejected() {
        when(solveSessionRepository.findById("nope")).thenReturn(Optional.empty());

        assertRejected(() -> guard.verify("nope", "conquest", "park"));
    }

    @Test
    void sessionOfAnotherProblemIsRejected() {
        givenSession("s1", "park");

        assertRejected(() -> guard.verify("s1", "distance", "park"));
    }

    @Test
    void sessionOfAnotherUserIsRejected() {
        givenSession("s1", "kim");

        assertRejected(() -> guard.verify("s1", "conquest", "park"));
    }
}
