package com.example.springboot.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * GET /me · POST /auth/login · POST /auth/signup 응답 (연동 문서 §2.1).
 * <p>
 * 시즌 관련 필드(seasonTier/seasonScore/seasonRank)는 <b>시즌 미배치 기본값</b>으로 내려간다.
 * 계약상 seasonTier/seasonRank 는 null 허용(첫 문제 풀기 전). 랭킹 연동(요건 9) 시
 * season_ranking 을 handle 로 조회해 채운다.
 */
@Getter
@AllArgsConstructor
public class MeResponseDTO {
    private String handle;
    private String role;          // USER | ADMIN — 프론트 관리자 탭 노출 판단용
    private String joinedAt;      // ISO 8601
    private Object seasonTier;    // TierRank {name, level} | null — 현재 항상 null(미배치)
    private int seasonScore;
    private Integer seasonRank;   // null 허용
    private String selectedTitleId;

    /** 시즌 미배치 유저(신규/랭킹 연동 전) 기본 응답. */
    public static MeResponseDTO of(UserDTO user) {
        return new MeResponseDTO(
                user.getHandle(),
                user.getRole().name(),
                user.getJoinedAt().toString(),
                null,
                0,
                null,
                user.getSelectedTitleId());
    }
}
