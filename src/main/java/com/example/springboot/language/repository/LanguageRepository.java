package com.example.springboot.language.repository;

import com.example.springboot.language.entity.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanguageRepository extends JpaRepository<LanguageEntity, String> {

    /** 활성 언어 — IDE 셀렉터 노출 순서대로 */
    List<LanguageEntity> findByEnabledTrueOrderBySortOrderAsc();
}
