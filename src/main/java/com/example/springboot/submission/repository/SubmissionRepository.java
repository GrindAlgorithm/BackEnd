package com.example.springboot.submission.repository;

import com.example.springboot.submission.entity.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 채점 현황 조회. MVP는 최근 50건(연동 문서 §2.12).
 * 시즌 필터는 제출 → 문제 → 시즌 경로로 건다.
 */
public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {

    List<SubmissionEntity> findTop50ByProblem_Season_IdOrderBySubmittedAtDesc(Integer seasonId);

    List<SubmissionEntity> findTop50ByProblem_Season_IdAndProblem_ProblemIdOrderBySubmittedAtDesc(Integer seasonId, String problemId);

    List<SubmissionEntity> findTop50ByProblem_ProblemIdOrderBySubmittedAtDesc(String problemId);

    List<SubmissionEntity> findTop50ByOrderBySubmittedAtDesc();
}
