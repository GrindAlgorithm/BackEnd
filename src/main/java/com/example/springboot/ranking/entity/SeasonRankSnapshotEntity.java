package com.example.springboot.ranking.entity;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 시즌 순위 일별 스냅샷 — snapshotDate 마감 기준 순위 (rank-snapshot-schema.sql) */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "season_rank_snapshot")
public class SeasonRankSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season_id", nullable = false)
    private Integer seasonId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false, length = 64)
    private String handle;

    @Column(name = "rank_no", nullable = false)
    private int rankNo;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_name", nullable = false, length = 16)
    private TierName tierName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_level", nullable = false, length = 4)
    private TierLevel tierLevel;

    public static SeasonRankSnapshotEntity createSnapshotEntity(Integer seasonId, LocalDate snapshotDate, String handle,
                                                                int rankNo, int score,
                                                                TierName tierName, TierLevel tierLevel) {
        return new SeasonRankSnapshotEntity(null, seasonId, snapshotDate, handle, rankNo, score, tierName, tierLevel);
    }
}
