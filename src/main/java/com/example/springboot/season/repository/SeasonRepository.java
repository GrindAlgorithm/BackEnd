package com.example.springboot.season.repository;

import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<SeasonEntity, Integer> {

    /** 최신(현재) 시즌 먼저 — 연동 문서 §2.5 정렬 규칙 */
    List<SeasonEntity> findAllByOrderByIdDesc();

    /** 현재 시즌 (홈 대시보드용) */
    Optional<SeasonEntity> findFirstByStatusOrderByIdDesc(SeasonStatus status);
}
