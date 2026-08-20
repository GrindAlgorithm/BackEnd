package com.example.springboot.notice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공지. MVP는 별도 공지 CRUD 없이 시드 데이터로 운용(어드민 Deferred B4) — 연동 문서 §2.4.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notice")
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String tag; // "공지" | "업데이트" 등

    @Column(nullable = false)
    private String title;

    /** 마크다운 본문 (요건 3) — 목록 응답에는 내리지 않고 상세/관리 화면에서만 사용 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private boolean highlight;

    public static NoticeEntity createNoticeEntity(String tag, String title, String body,
                                                  LocalDateTime publishedAt, boolean highlight) {
        return new NoticeEntity(null, tag, title, body, publishedAt, highlight);
    }

    /** 관리자 수정 — 게시 시각(publishedAt)은 작성 시점을 유지한다. */
    public void update(String tag, String title, String body, boolean highlight) {
        this.tag = tag;
        this.title = title;
        this.body = body;
        this.highlight = highlight;
    }
}
