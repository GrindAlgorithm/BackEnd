package com.example.springboot.problem.repository;

import com.example.springboot.problem.entity.ProblemTestcaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemTestcaseRepository extends JpaRepository<ProblemTestcaseEntity, Long> {

    /** 채점 순서대로 — 공개(hidden=0)·히든(hidden=1) 전부 (채점 전용, API 노출 금지) */
    List<ProblemTestcaseEntity> findByProblem_ProblemIdOrderByOrdinalAsc(String problemId);
}
