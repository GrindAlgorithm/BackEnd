package com.example.springboot.problem.repository;

import com.example.springboot.problem.entity.ProblemBodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProblemBodyRepository extends JpaRepository<ProblemBodyEntity, Long> {

    /** URL 키로 본문 조회 */
    Optional<ProblemBodyEntity> findByProblem_ProblemId(String problemId);
}
