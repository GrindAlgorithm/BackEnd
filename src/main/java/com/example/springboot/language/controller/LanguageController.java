package com.example.springboot.language.controller;

import com.example.springboot.language.dto.LanguageDTO;
import com.example.springboot.language.service.LanguageService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 지원 언어 목록 (요건 24) — IDE 언어 셀렉터가 사용. */
@RestController
@RequestMapping("/api/v1/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping("")
    public ResponseResult<List<LanguageDTO>> list() {
        return ResponseResult.success(languageService.getEnabledLanguages());
    }
}
