package com.example.springboot.language.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.language.dto.LanguageDTO;
import com.example.springboot.language.entity.LanguageEntity;
import com.example.springboot.language.repository.LanguageRepository;
import com.example.springboot.submission.entity.LanguageCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;

    @Override
    public List<LanguageDTO> getEnabledLanguages() {
        return languageRepository.findByEnabledTrueOrderBySortOrderAsc().stream()
                .map(LanguageDTO::of)
                .toList();
    }

    @Override
    public int resolveJudge0Id(LanguageCode language) {
        LanguageEntity row = languageRepository.findById(language.getValue()).orElse(null);
        if (row == null) {
            // 테이블 미적용 환경 폴백 — enum 기본값으로 동작을 유지한다
            log.warn("resolveJudge0Id: language 테이블에 {} 없음 — enum 기본값({}) 사용",
                    language.getValue(), language.getJudge0Id());
            return language.getJudge0Id();
        }
        if (!row.isEnabled()) {
            throw ApiException.badRequest("UNSUPPORTED_LANGUAGE", "지원하지 않는 언어입니다: " + row.getLabel());
        }
        return row.getJudge0Id();
    }
}
