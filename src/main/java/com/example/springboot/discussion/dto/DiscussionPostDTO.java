package com.example.springboot.discussion.dto;

import com.example.springboot.common.tier.TierName;
import com.example.springboot.discussion.entity.DiscussionCategory;
import com.example.springboot.discussion.entity.DiscussionPostEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZoneOffset;

/** 연동 문서 §2.14 posts[] (DiscussionPost). */
@Getter
@AllArgsConstructor
public class DiscussionPostDTO {

    // KST 고정 — 시각은 ISO 8601(+09:00) 노출 (연동 문서 §1.1)
    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    private Long id;
    private DiscussionCategory category;
    private String title;
    private AuthorDTO author;
    private int commentCount;
    private int voteCount;
    private String createdAt; // ISO 8601 (+09:00)

    public static DiscussionPostDTO of(DiscussionPostEntity e) {
        return new DiscussionPostDTO(
                e.getId(),
                e.getCategory(),
                e.getTitle(),
                new AuthorDTO(e.getAuthorHandle(), e.getAuthorTierName()),
                e.getCommentCount(),
                e.getVoteCount(),
                e.getCreatedAt().atOffset(KST).toString());
    }

    /** author.tierName 은 레벨 없이 티어명만(작성자 옆 색 점용) — 연동 문서 §2.14 */
    @Getter
    @AllArgsConstructor
    public static class AuthorDTO {
        private String handle;
        private TierName tierName;
    }
}
