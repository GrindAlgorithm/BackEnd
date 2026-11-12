package com.example.springboot.user.dto;

import com.example.springboot.dashboard.dto.DecayWarningDTO;
import com.example.springboot.ranking.dto.RankHistoryDTO;
import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.dashboard.dto.ActivityCalendarDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/** 유저 프로필 서비스 모델 (연동 문서 §2.15). 표현 변환은 {@link UserProfileResponseDTO} 에서 수행. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String handle;
    private LocalDateTime joinedAt;
    private boolean isMe;

    /** 현재 시즌 미배치(랭킹 행 없음)면 null */
    private TierName seasonTierName;
    private TierLevel seasonTierLevel;
    private int seasonScore;
    private Integer seasonRank; // 미배치면 null

    private UserStatsDTO stats;
    private String selectedTitleId;
    private ActivityCalendarDTO activity; // 최근 52주, 과거→오늘 순
    private List<RecentSolvedItemDTO> recentSolved;
    private List<RecentSubmissionItemDTO> recentSubmissions;
    /** 하락 예고 — 본인 프로필에만, 곧 하락할 일이 없으면 null (§2.15 decay) */
    private DecayWarningDTO decay;
    /** 본인: 보유+미보유 전체 / 타인: 보유만 (§2.15 titles) */
    private List<TitleResponseDTO> titles;
    /** 현재 시즌 순위 추이, 미배치면 null (§2.15 rankHistory) */
    private RankHistoryDTO rankHistory;
}
