package com.example.springboot.submission.dto;

import com.example.springboot.submission.entity.LanguageCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §2.9 POST /runs 요청 (코드 실행 — 채점 아님). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RunRequestDTO {
    private String problemId;
    private String solveSessionId;
    private LanguageCode language;
    private String sourceCode;
    private String stdin;
}
