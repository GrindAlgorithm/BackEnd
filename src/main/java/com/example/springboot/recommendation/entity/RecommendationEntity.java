package com.example.springboot.recommendation.entity;

import com.example.springboot.problem.entity.ProblemEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오늘의 추천 큐레이션 — 연동 문서 §2.4 todayPicks.
 * 추천 로직은 Deferred라 rank_no 순위로 관리한다(오름차순 노출).
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recommendation")
public class RecommendationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemEntity problem;

    @Column(name = "rank_no", nullable = false)
    private int rankNo;

    @Column(nullable = false, length = 64)
    private String reason;

    @Column(name = "reason_type", nullable = false, length = 32)
    private String reasonType;
}
