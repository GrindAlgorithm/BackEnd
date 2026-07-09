package com.example.springboot.problem.repository;

import com.example.springboot.problem.entity.ProblemSampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemSampleRepository extends JpaRepository<ProblemSampleEntity, Long> {

    /** URL 키로 예제 목록 조회 (순번 순) */
    List<ProblemSampleEntity> findByProblem_ProblemIdOrderByOrdinalAsc(String problemId);
}
