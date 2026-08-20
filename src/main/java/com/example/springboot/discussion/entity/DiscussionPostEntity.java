package com.example.springboot.discussion.entity;

import com.example.springboot.common.tier.TierName;
import com.example.springboot.problem.entity.ProblemEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 토론 글 — 연동 문서 §2.14 DiscussionPost.
 * 작성은 요건 4 로 구현(댓글/투표는 Deferred). 작성자는 handle + 티어 스냅샷으로 보관한다.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discussion_post")
public class DiscussionPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemEntity problem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DiscussionCategory category;

    @Column(nullable = false)
    private String title;

    /** 마크다운 본문 (요건 4) — 목록 응답에는 내리지 않고 상세에서만 사용 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "author_handle", nullable = false, length = 64)
    private String authorHandle;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_tier_name", nullable = false, length = 16)
    private TierName authorTierName;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(name = "vote_count", nullable = false)
    private int voteCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static DiscussionPostEntity createDiscussionPostEntity(ProblemEntity problem,
                                                                  DiscussionCategory category,
                                                                  String title, String body,
                                                                  String authorHandle,
                                                                  TierName authorTierName,
                                                                  LocalDateTime createdAt) {
        return new DiscussionPostEntity(null, problem, category, title, body,
                authorHandle, authorTierName, 0, 0, createdAt);
    }
}
