package com.example.springboot.ranking.repository;

import com.example.springboot.ranking.entity.SeasonRankSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SeasonRankSnapshotRepository extends JpaRepository<SeasonRankSnapshotEntity, Long> {

    /** 그날 스냅샷이 이미 있는지 — 자정·기동 중복 실행 방지 */
    boolean existsBySeasonIdAndSnapshotDate(Integer seasonId, LocalDate snapshotDate);

    /** 특정 날짜 마감 순위표 전체 — 7일 순위 변동(§2.4 weeklyDelta) */
    List<SeasonRankSnapshotEntity> findBySeasonIdAndSnapshotDate(Integer seasonId, LocalDate snapshotDate);

    /** 한 유저의 시즌 순위 추이(과거→최근) — 프로필 rankHistory(§2.15) */
    List<SeasonRankSnapshotEntity> findBySeasonIdAndHandleOrderBySnapshotDateAsc(Integer seasonId, String handle);
}
