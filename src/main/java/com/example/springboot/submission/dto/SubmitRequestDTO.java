package com.example.springboot.submission.dto;

import com.example.springboot.submission.entity.LanguageCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §2.10 POST /submissions 요청 (비동기 채점 시작). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRequestDTO {
    private String problemId;
    private String solveSessionId;
    private LanguageCode language;
    private String sourceCode;
}
