package com.example.springboot.ranking.repository;

import com.example.springboot.ranking.entity.SeasonRankingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonRankingRepository extends JpaRepository<SeasonRankingEntity, Long> {

    /** 특정 시즌 랭킹 — 점수 내림차순(= 순위 순). 연동 문서 §2.13 season scope */
    List<SeasonRankingEntity> findBySeason_IdOrderByScoreDesc(Integer seasonId);

    /** 전체 시즌 랭킹 — overall 집계용(핸들별 합산은 서비스에서) */
    List<SeasonRankingEntity> findAllByOrderByScoreDesc();
}
