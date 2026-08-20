package com.example.springboot.integrity.repository;

import com.example.springboot.integrity.entity.SolveEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolveEventRepository extends JpaRepository<SolveEventEntity, Long> {

    /** 세션당 적재 상한 검사용 (§2.17 남용 방지) */
    long countBySolveSessionId(String solveSessionId);
}
