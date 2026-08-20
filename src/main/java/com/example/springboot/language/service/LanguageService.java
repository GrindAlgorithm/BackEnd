package com.example.springboot.language.service;

import com.example.springboot.language.dto.LanguageDTO;
import com.example.springboot.submission.entity.LanguageCode;

import java.util.List;

/** 지원 언어 (요건 24). 활성 목록 조회 + 채점/실행용 judge0 id 해석. */
public interface LanguageService {

    List<LanguageDTO> getEnabledLanguages();

    /**
     * 채점/실행에 쓸 Judge0 language_id — language 테이블 값 우선, 행이 없으면 enum 기본값.
     * 비활성(enabled=0) 언어면 400 UNSUPPORTED_LANGUAGE.
     */
    int resolveJudge0Id(LanguageCode language);
}
