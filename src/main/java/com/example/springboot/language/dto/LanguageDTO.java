package com.example.springboot.language.dto;

import com.example.springboot.language.entity.LanguageEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** GET /languages 응답 항목 (요건 24). judge0Id 는 서버 내부 값이라 노출하지 않는다. */
@Getter
@AllArgsConstructor
public class LanguageDTO {
    private String code;  // java11 | python3 | ...
    private String label; // 표시명

    public static LanguageDTO of(LanguageEntity e) {
        return new LanguageDTO(e.getCode(), e.getLabel());
    }
}
