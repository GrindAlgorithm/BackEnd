package com.example.springboot.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 칭호 — 연동 문서 §2.15 titles[] (UserTitle).
 * 칭호 자동 발급 규칙(A4)은 "골드 이상부터"만 확정 — 발급 도메인 구현 전까지 빈 배열로만 내려간다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TitleResponseDTO {
    private String id;
    private String name;
    private String description;
    private String colorKey;   // bronze|silver|gold|platinum|diamond|green|blue
    private boolean owned;
    private Integer fromSeason; // null = 시즌 무관
    private boolean expired;
}
