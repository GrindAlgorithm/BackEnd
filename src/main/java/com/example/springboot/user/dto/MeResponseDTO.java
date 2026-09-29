package com.example.springboot.user.dto;

import com.example.springboot.problem.dto.TierRankDTO;
import com.example.springboot.ranking.dto.RankingEntryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * GET /me · POST /auth/login · POST /auth/signup 응답 (연동 문서 §2.1).
 * <p>
 * 시즌 관련 필드(seasonTier/seasonScore/seasonRank)는 현재 시즌 랭킹 기준. 계약상
 * seasonTier/seasonRank 는 null 허용 — 시즌 미배치(첫 정답 전) 유저는 null/0/null.
 */
@Getter
@AllArgsConstructor
public class MeResponseDTO {
    private String handle;
    private String role;          // USER | ADMIN — 프론트 관리자 탭 노출 판단용
    private String joinedAt;      // ISO 8601
    private TierRankDTO seasonTier; // TierRank {name, level} | null(미배치)
    private int seasonScore;
    private Integer seasonRank;   // null 허용
    private String selectedTitleId;

    /** @param seasonEntry 현재 시즌 내 순위 항목. 미배치면 null */
    public static MeResponseDTO of(UserDTO user, RankingEntryDTO seasonEntry) {
        return new MeResponseDTO(
                user.getHandle(),
                user.getRole().name(),
                user.getJoinedAt().toString(),
                seasonEntry == null ? null : TierRankDTO.of(seasonEntry.getTierName(), seasonEntry.getTierLevel()),
                seasonEntry == null ? 0 : seasonEntry.getScore(),
                seasonEntry == null ? null : seasonEntry.getRank(),
                user.getSelectedTitleId());
    }
}
