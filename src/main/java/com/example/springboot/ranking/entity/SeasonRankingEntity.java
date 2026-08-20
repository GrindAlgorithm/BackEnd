package com.example.springboot.ranking.entity;

import com.example.springboot.common.tier.TierCut;
import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.season.entity.SeasonEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 시즌별 랭킹 스냅샷 (연동 문서 §2.13).
 * 유저 도메인 연동 전이라 대상은 handle 문자열. 순위는 점수 내림차순으로 조회 시 계산.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "season_ranking")
public class SeasonRankingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private SeasonEntity season;

    @Column(nullable = false, length = 64)
    private String handle;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_name", nullable = false, length = 16)
    private TierName tierName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_level", nullable = false, length = 4)
    private TierLevel tierLevel;

    @Column(nullable = false)
    private int score;

    @Column(name = "solved_count", nullable = false)
    private int solvedCount;

    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    public static SeasonRankingEntity createSeasonRankingEntity(SeasonEntity season, String handle,
                                                               TierName tierName, TierLevel tierLevel,
                                                               int score, int solvedCount, LocalDateTime lastActiveAt) {
        return new SeasonRankingEntity(null, season, handle, tierName, tierLevel, score, solvedCount, lastActiveAt);
    }

    /** 신규 진입 행 — 첫 정답 반영 직전 상태(브론즈 V, 0점). */
    public static SeasonRankingEntity createUnrankedEntity(SeasonEntity season, String handle, LocalDateTime at) {
        return new SeasonRankingEntity(null, season, handle, TierName.BRONZE, TierLevel.V, 0, 0, at);
    }

    /** 첫 정답 반영 — 점수 가산·풀이 수 증가 후 누적 점수로 티어 재계산 (채점→랭킹 연동). */
    public void applyAccepted(int points, LocalDateTime at) {
        this.score += points;
        this.solvedCount += 1;
        this.lastActiveAt = at;
        this.tierName = TierCut.nameOf(this.score);
        this.tierLevel = TierCut.levelOf(this.score);
    }
}
