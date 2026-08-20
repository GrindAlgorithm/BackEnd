package com.example.springboot.discussion.dto;

import com.example.springboot.discussion.entity.DiscussionCategory;
import com.example.springboot.discussion.entity.DiscussionPostEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZoneOffset;

/** 토론 글 상세 — 목록용 DiscussionPostDTO 에 마크다운 body 를 더한 형태 (요건 4). */
@Getter
@AllArgsConstructor
public class DiscussionPostDetailDTO {

    // KST 고정 — 시각은 ISO 8601(+09:00) 노출 (연동 문서 §1.1)
    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    private Long id;
    private DiscussionCategory category;
    private String title;
    private String body;                       // 마크다운 원문 — 렌더링은 프론트 책임
    private DiscussionPostDTO.AuthorDTO author;
    private int commentCount;
    private int voteCount;
    private String createdAt;                  // ISO 8601 (+09:00)

    public static DiscussionPostDetailDTO of(DiscussionPostEntity e) {
        return new DiscussionPostDetailDTO(
                e.getId(),
                e.getCategory(),
                e.getTitle(),
                e.getBody(),
                new DiscussionPostDTO.AuthorDTO(e.getAuthorHandle(), e.getAuthorTierName()),
                e.getCommentCount(),
                e.getVoteCount(),
                e.getCreatedAt().atOffset(KST).toString());
    }
}
