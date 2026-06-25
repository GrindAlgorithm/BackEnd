package com.example.springboot.problem.repository;

import com.example.springboot.problem.entity.ProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {

    /** 특정 시즌의 문제 목록 (번호 순) */
    List<ProblemEntity> findBySeason_IdOrderByDisplayNoAsc(Integer seasonId);
}
