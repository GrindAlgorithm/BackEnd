package com.example.springboot.ranking.repository;

import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.season.entity.SeasonStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeasonRankingRepository extends JpaRepository<SeasonRankingEntity, Long> {

    /** 특정 시즌 랭킹 — 점수 내림차순(= 순위 순). 연동 문서 §2.13 season scope */
    List<SeasonRankingEntity> findBySeason_IdOrderByScoreDesc(Integer seasonId);

    /** 전체 시즌 랭킹 — overall 집계용(핸들별 합산은 서비스에서) */
    List<SeasonRankingEntity> findAllByOrderByScoreDesc();

    /** 현재 시즌의 내 랭킹 행 — 토론 작성자 티어 스냅샷용 (요건 4). 미배치면 empty */
    Optional<SeasonRankingEntity> findFirstBySeason_StatusAndHandle(SeasonStatus status, String handle);

    /**
     * 랭킹 행이 없을 때만 미배치 행을 만든다 — 이미 있으면(uq_season_handle) 아무것도 바꾸지 않는다.
     * "조회 후 없으면 insert" 는 동시 첫 정답끼리 중복키로 터지므로 DB 가 판정하게 한다.
     * <p>
     * INSERT IGNORE 가 아니라 ON DUPLICATE KEY UPDATE 인 이유: IGNORE 는 중복 행에 공유(S) 잠금을 걸어
     * 뒤이은 FOR UPDATE 가 S→X 승격을 하게 되고, 동시에 대기하던 두 트랜잭션이 서로의 S 잠금을 기다리는
     * 교착이 난다(실측). ODKU 는 중복 행에 처음부터 배타(X) 잠금을 잡으므로 승격이 없다.
     */
    @Modifying
    @Query(value = "INSERT INTO season_ranking "
            + "(season_id, handle, tier_name, tier_level, score, solved_count, last_active_at) "
            + "VALUES (:seasonId, :handle, :tierName, :tierLevel, 0, 0, :at) "
            + "ON DUPLICATE KEY UPDATE handle = handle", nativeQuery = true)
    int insertIfAbsent(@Param("seasonId") Integer seasonId, @Param("handle") String handle,
                       @Param("tierName") String tierName, @Param("tierLevel") String tierLevel,
                       @Param("at") LocalDateTime at);

    /** 랭킹 행 쓰기 잠금(SELECT … FOR UPDATE) — 같은 유저의 정답 반영을 직렬화한다 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM SeasonRankingEntity r WHERE r.season.id = :seasonId AND r.handle = :handle")
    Optional<SeasonRankingEntity> findForUpdate(@Param("seasonId") Integer seasonId, @Param("handle") String handle);
}
