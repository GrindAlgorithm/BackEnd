package com.example.springboot.user.dto;

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
 * decay 는 하락 공식 미확정(A1) → 항상 null, titles 는 칭호 발급(A4) 미구현 → 빈 배열.
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
    private Object decay;            // 항상 null (§2.4 decay 공식 미확정)
    private UserStatsDTO stats;
    private List<TitleResponseDTO> titles;
    private String selectedTitleId;
    private ActivityCalendarDTO activity;
    private List<RecentSolvedItemDTO> recentSolved;
    private List<RecentSubmissionItemDTO> recentSubmissions;
    /** 로그인 열람자 기준 관계(§2.18). 비로그인이면 null → 프론트가 친구 버튼을 숨긴다 */
    private FriendRelation friendRelation;
    private Long friendRequestId;    // friendRelation 이 incoming/outgoing 일 때만

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
                null,
                profile.getStats(),
                List.of(),
                profile.getSelectedTitleId(),
                profile.getActivity(),
                profile.getRecentSolved(),
                profile.getRecentSubmissions(),
                null,
                null
        );
    }
}
