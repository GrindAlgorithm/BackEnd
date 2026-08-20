package com.example.springboot.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 관리자 공지 작성/수정 요청 (POST·PUT /admin/notices). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeRequestDTO {

    @NotBlank(message = "태그를 입력해 주세요")
    @Size(max = 32, message = "태그는 32자 이하여야 합니다")
    private String tag; // "공지" | "업데이트" 등

    @NotBlank(message = "제목을 입력해 주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    private String title;

    /** 마크다운 본문 — 제목만 있는 공지도 허용하므로 선택 (null → 빈 본문) */
    @Size(max = 20_000, message = "본문은 20,000자 이하여야 합니다")
    private String body;

    /** 상단 강조 여부 (기본 false) */
    private boolean highlight;
}
