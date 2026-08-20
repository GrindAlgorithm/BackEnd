package com.example.springboot.notice.dto;

import com.example.springboot.notice.entity.NoticeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZoneOffset;

/**
 * 공지 상세 — 목록용 NoticeDTO 에 마크다운 body 를 더한 형태 (요건 3).
 * 관리 화면(수정 폼)과 공지 상세 조회(GET /notices/{id})에서 사용한다.
 */
@Getter
@AllArgsConstructor
public class NoticeDetailDTO {
    private Long id;
    private String tag;
    private String title;
    private String body;        // 마크다운 원문 — 렌더링은 프론트 책임
    private String publishedAt; // ISO 8601 (+09:00)
    private boolean highlight;

    // KST 고정 — 스펙상 시각은 ISO 8601(+09:00)로 노출 (연동 문서 §1.1)
    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    public static NoticeDetailDTO of(NoticeEntity notice) {
        return new NoticeDetailDTO(
                notice.getId(),
                notice.getTag(),
                notice.getTitle(),
                notice.getBody(),
                notice.getPublishedAt().atOffset(KST).toString(),
                notice.isHighlight()
        );
    }
}
