package com.example.springboot.user.dto;

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
}
