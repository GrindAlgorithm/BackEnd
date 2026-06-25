package com.example.springboot.problem.entity;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.season.entity.SeasonEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "problem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProblemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** URL 키 (예: "S2-08", "21609") — 내부 PK(id)와 분리. 연동 문서 §1.5 */
    @Column(name = "problem_id", nullable = false, unique = true, length = 32)
    private String problemId;

    @Column(name = "display_no", nullable = false, length = 32)
    private String displayNo;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_name", nullable = false, length = 16)
    private TierName tierName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_level", nullable = false, length = 4)
    private TierLevel tierLevel;

    /** 시즌 문제는 소속 시즌, 일반 문제는 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private SeasonEntity season;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "problem_tag", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "tag", nullable = false, length = 64)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // 정답률(acceptanceRate) 계산용 누적 통계 — 연동 문서 §2.7 stats
    @Column(name = "submission_count", nullable = false)
    @Builder.Default
    private long submissionCount = 0L;

    @Column(name = "accepted_count", nullable = false)
    @Builder.Default
    private long acceptedCount = 0L;

    @Column(name = "solver_count", nullable = false)
    @Builder.Default
    private long solverCount = 0L;
}
