package com.example.springboot.problem.repository;

import com.example.springboot.problem.entity.ProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<ProblemEntity, Long> {

    /** 특정 시즌의 문제 목록 (번호 순) */
    List<ProblemEntity> findBySeason_IdOrderByDisplayNoAsc(Integer seasonId);

    /**
     * URL 키로 공개 문제 단건 조회 — 공개 전(UPCOMING) 시즌 문제는 없는 것으로 취급한다.
     * 유저 요청(상세·본문 열람·실행·제출·토론)은 반드시 이것을 쓴다.
     */
    @Query("SELECT p FROM ProblemEntity p LEFT JOIN p.season s WHERE p.problemId = :problemId "
            + "AND (s IS NULL OR s.status <> com.example.springboot.season.entity.SeasonStatus.UPCOMING)")
    Optional<ProblemEntity> findPublicByProblemId(@Param("problemId") String problemId);

    /** 특정 시즌의 문제 수 (시즌 진행률 totalCount) */
    long countBySeason_Id(Integer seasonId);

    /**
     * 채점 종결 시 문제 통계 원자적 증분 — 엔티티 필드를 읽어 +1 후 덮어쓰면 동시 채점끼리 갱신이 유실된다.
     *
     * @param accepted  정답이면 1, 아니면 0
     * @param newSolver 이 유저의 첫 정답이면 1 (맞힌 사람 수는 유저당 한 번만 센다)
     */
    @Modifying
    @Query("UPDATE ProblemEntity p SET p.submissionCount = p.submissionCount + 1, "
            + "p.acceptedCount = p.acceptedCount + :accepted, p.solverCount = p.solverCount + :newSolver "
            + "WHERE p.id = :id")
    int incrementStats(@Param("id") Long id, @Param("accepted") long accepted, @Param("newSolver") long newSolver);
}
