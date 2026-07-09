package com.example.springboot.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** IDE 진입(본문 열람) 서비스 모델 — 연동 문서 §2.8. 표현 변환은 {@link OpenProblemResponseDTO}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenProblemDTO {
    private String solveSessionId;
    private LocalDateTime openedAt;
    private ProblemDetailDTO problem;
    private ProblemBodyDTO body;
}
