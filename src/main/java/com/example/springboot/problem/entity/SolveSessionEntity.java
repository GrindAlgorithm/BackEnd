package com.example.springboot.problem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 본문 열람 세션 = 풀이 시작 시각 기록(B2, 연동 문서 §2.8).
 * 이후 POST /runs · POST /submissions 가 이 세션(solveSessionId)에 연결된다.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "solve_session")
public class SolveSessionEntity {

    /** UUID */
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemEntity problem;

    /** 인증 연동 전 'anonymous' */
    @Column(name = "user_handle", nullable = false, length = 64)
    private String userHandle;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    public static SolveSessionEntity createSolveSessionEntity(String id, ProblemEntity problem,
                                                              String userHandle, LocalDateTime openedAt) {
        return new SolveSessionEntity(id, problem, userHandle, openedAt);
    }
}
