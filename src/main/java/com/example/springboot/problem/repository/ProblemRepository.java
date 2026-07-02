package com.example.springboot.problem.repository;

import com.example.springboot.problem.entity.ProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {

    /** 특정 시즌의 문제 목록 (번호 순) */
    List<ProblemEntity> findBySeason_IdOrderByDisplayNoAsc(Integer seasonId);

    /** URL 키로 문제 단건 조회 (문제 상세) */
    Optional<ProblemEntity> findByProblemId(String problemId);

    /** 특정 시즌의 문제 수 (시즌 진행률 totalCount) */
    long countBySeason_Id(Integer seasonId);
}
