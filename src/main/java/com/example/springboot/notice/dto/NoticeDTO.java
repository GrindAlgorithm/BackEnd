package com.example.springboot.notice.dto;

import com.example.springboot.notice.entity.NoticeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneOffset;

/** 연동 문서 §2.4 dashboard.notices[] (Notice) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDTO {
    private Long id;
    private String tag;
    private String title;
    private String publishedAt; // ISO 8601 (+09:00)
    private boolean highlight;

    // KST 고정 — 스펙상 시각은 ISO 8601(+09:00)로 노출 (연동 문서 §1.1)
    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    public static NoticeDTO of(NoticeEntity notice) {
        return new NoticeDTO(
                notice.getId(),
                notice.getTag(),
                notice.getTitle(),
                notice.getPublishedAt().atOffset(KST).toString(),
                notice.isHighlight()
        );
    }
}
