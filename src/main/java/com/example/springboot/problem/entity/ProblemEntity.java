package com.example.springboot.problem.entity;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.season.entity.SeasonEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "problem")
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
    private List<String> tags = new ArrayList<>();

    // 문제 상세 노출 메타 — 연동 문서 §2.7
    @Column(name = "time_limit_sec", nullable = false)
    private int timeLimitSec;

    @Column(name = "memory_limit_mb", nullable = false)
    private int memoryLimitMb;

    /** 예상 시간복잡도(노출 허용 메타). 없으면 null */
    @Column(name = "expected_complexity")
    private String expectedComplexity;

    // 정답률(acceptanceRate) 계산용 누적 통계 — 연동 문서 §2.7 stats
    @Column(name = "submission_count", nullable = false)
    private long submissionCount;

    @Column(name = "accepted_count", nullable = false)
    private long acceptedCount;

    @Column(name = "solver_count", nullable = false)
    private long solverCount;

    @Column(name = "discussion_count", nullable = false)
    private int discussionCount;

    public static ProblemEntity createProblemEntity(String problemId, String displayNo, String title,
                                                    TierName tierName, TierLevel tierLevel, SeasonEntity season, List<String> tags,
                                                    int timeLimitSec, int memoryLimitMb, String expectedComplexity,
                                                    long submissionCount, long acceptedCount, long solverCount, int discussionCount) {
        return new ProblemEntity(null, problemId, displayNo, title, tierName, tierLevel, season, tags,
                timeLimitSec, memoryLimitMb, expectedComplexity, submissionCount, acceptedCount, solverCount, discussionCount);
    }

    /**
     * 채점 완료 시 통계 갱신. 제출 수는 항상 +1, 정답이면 정답 수·맞힌 사람 수도 +1.
     * (유저 도메인 연동 전이라 맞힌 사람 중복 제거는 생략 — accepted마다 solver_count 증가)
     */
    public void recordSubmission(boolean accepted) {
        this.submissionCount++;
        if (accepted) {
            this.acceptedCount++;
            this.solverCount++;
        }
    }

    /** 정답 비율 (%, 소수 첫째 자리). 제출 없으면 0 — 연동 문서 §2.6/§2.7 */
    public double acceptanceRate() {
        if (submissionCount <= 0) {
            return 0.0;
        }
        return Math.round(acceptedCount * 1000.0 / submissionCount) / 10.0;
    }
}
