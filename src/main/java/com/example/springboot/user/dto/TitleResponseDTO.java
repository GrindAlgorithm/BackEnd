package com.example.springboot.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 칭호 — 연동 문서 §2.15 titles[] (UserTitle).
 * 칭호 정의 = 시즌 리워드(season_reward), 발급 = TitleService (리워드 달성 + 시즌 점수 기준 골드 이상).
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
