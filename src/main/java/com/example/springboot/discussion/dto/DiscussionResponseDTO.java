package com.example.springboot.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 연동 문서 §2.14 — 403 대신 200 + accessible 분기.
 * 미해결(locked): stats 만. 정답자(unlocked): firstSolvedAt + posts 까지.
 */
@Getter
@AllArgsConstructor
public class DiscussionResponseDTO {
    private boolean accessible;
    private String firstSolvedAt;              // null 허용 (미해결)
    private DiscussionStatsDTO stats;
    private List<DiscussionPostDTO> posts;     // null 허용 (미해결)

    /** 미해결 유저 — 통계만 노출. */
    public static DiscussionResponseDTO locked(DiscussionStatsDTO stats) {
        return new DiscussionResponseDTO(false, null, stats, null);
    }

    /** 정답자 — 글 목록까지 노출. */
    public static DiscussionResponseDTO unlocked(String firstSolvedAt, DiscussionStatsDTO stats,
                                                 List<DiscussionPostDTO> posts) {
        return new DiscussionResponseDTO(true, firstSolvedAt, stats, posts);
    }
}
