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
 * 글 작성은 Deferred라 현재 시드로만 운용. 작성자는 handle + 티어 스냅샷으로 보관한다.
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
}
