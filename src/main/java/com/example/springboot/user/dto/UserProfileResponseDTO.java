package com.example.springboot.user.dto;

import com.example.springboot.dashboard.dto.DecayWarningDTO;
import com.example.springboot.ranking.dto.RankHistoryDTO;
import com.example.springboot.dashboard.dto.ActivityCalendarDTO;
import com.example.springboot.friend.dto.FriendRelation;
import com.example.springboot.problem.dto.TierRankDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 연동 문서 §2.15 GET /users/{handle} 응답 (UserProfileResponse).
 * decay 는 본인에게만(곧 하락할 때), titles 는 본인=전체·타인=보유만, rankHistory 는 현재 시즌 순위 추이.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
    private String handle;
    private LocalDateTime joinedAt;
    /** Lombok boolean getter(isMe())를 Jackson 이 "me"로 줄이므로 계약(§2.15) 필드명을 고정한다 */
    @JsonProperty("isMe")
    private boolean isMe;
    private TierRankDTO seasonTier;  // 미배치면 null
    private int seasonScore;
    private Integer seasonRank;      // 미배치면 null
    private DecayWarningDTO decay;   // 본인 + 하락 예고가 있을 때만, 그 외 null
    private UserStatsDTO stats;
    private List<TitleResponseDTO> titles;
    private String selectedTitleId;
    private ActivityCalendarDTO activity;
    private List<RecentSolvedItemDTO> recentSolved;
    private List<RecentSubmissionItemDTO> recentSubmissions;
    /** 로그인 열람자 기준 관계(§2.18). 비로그인이면 null → 프론트가 친구 버튼을 숨긴다 */
    private FriendRelation friendRelation;
    private Long friendRequestId;    // friendRelation 이 incoming/outgoing 일 때만
    private RankHistoryDTO rankHistory; // 현재 시즌 순위 추이, 미배치면 null

    public static UserProfileResponseDTO of(UserProfileDTO profile) {
        TierRankDTO seasonTier = profile.getSeasonTierName() == null
                ? null
                : TierRankDTO.of(profile.getSeasonTierName(), profile.getSeasonTierLevel());
        return new UserProfileResponseDTO(
                profile.getHandle(),
                profile.getJoinedAt(),
                profile.isMe(),
                seasonTier,
                profile.getSeasonScore(),
                profile.getSeasonRank(),
                profile.getDecay(),
                profile.getStats(),
                profile.getTitles() == null ? List.of() : profile.getTitles(),
                profile.getSelectedTitleId(),
                profile.getActivity(),
                profile.getRecentSolved(),
                profile.getRecentSubmissions(),
                null,
                null,
                profile.getRankHistory()
        );
    }
}
