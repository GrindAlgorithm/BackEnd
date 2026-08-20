package com.example.springboot.submission.repository;

import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 채점 현황 조회. MVP는 최근 50건(연동 문서 §2.12).
 * 시즌 필터는 제출 → 문제 → 시즌 경로로 건다.
 */
public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {

    List<SubmissionEntity> findTop50ByProblem_Season_IdOrderBySubmittedAtDesc(Integer seasonId);

    List<SubmissionEntity> findTop50ByProblem_Season_IdAndProblem_ProblemIdOrderBySubmittedAtDesc(Integer seasonId, String problemId);

    List<SubmissionEntity> findTop50ByProblem_ProblemIdOrderBySubmittedAtDesc(String problemId);

    List<SubmissionEntity> findTop50ByOrderBySubmittedAtDesc();

    /** 특정 유저의 시즌 내 전체 제출 — 시즌 화면 myStatus/리워드 계산용 (GET /seasons/current) */
    List<SubmissionEntity> findByProblem_Season_IdAndUserHandle(Integer seasonId, String userHandle);

    /** 특정 유저가 해당 문제를 해결(Accepted)한 이력이 있는지 — 토론 접근 판정(§2.14) */
    boolean existsByProblem_ProblemIdAndUserHandleAndStatus(String problemId, String userHandle, SubmissionStatus status);

    /** 해당 문제 최초 해결(Accepted) 제출 — firstSolvedAt(§2.14) */
    Optional<SubmissionEntity> findFirstByProblem_ProblemIdAndUserHandleAndStatusOrderBySubmittedAtAsc(
            String problemId, String userHandle, SubmissionStatus status);
}
