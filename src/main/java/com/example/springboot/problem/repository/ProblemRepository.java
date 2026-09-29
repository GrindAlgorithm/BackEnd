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

    /** URL 키로 문제 단건 조회 (문제 상세) */
    Optional<ProblemEntity> findByProblemId(String problemId);

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
