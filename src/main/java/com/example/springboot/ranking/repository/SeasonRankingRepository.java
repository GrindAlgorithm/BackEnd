package com.example.springboot.ranking.repository;

import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.season.entity.SeasonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeasonRankingRepository extends JpaRepository<SeasonRankingEntity, Long> {

    /** 특정 시즌 랭킹 — 점수 내림차순(= 순위 순). 연동 문서 §2.13 season scope */
    List<SeasonRankingEntity> findBySeason_IdOrderByScoreDesc(Integer seasonId);

    /** 전체 시즌 랭킹 — overall 집계용(핸들별 합산은 서비스에서) */
    List<SeasonRankingEntity> findAllByOrderByScoreDesc();

    /** 현재 시즌의 내 랭킹 행 — 토론 작성자 티어 스냅샷용 (요건 4). 미배치면 empty */
    Optional<SeasonRankingEntity> findFirstBySeason_StatusAndHandle(SeasonStatus status, String handle);

    /** 특정 시즌의 유저 행 — 채점→랭킹 반영 upsert 용 */
    Optional<SeasonRankingEntity> findFirstBySeason_IdAndHandle(Integer seasonId, String handle);
}
