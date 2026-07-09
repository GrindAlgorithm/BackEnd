package com.example.springboot.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneOffset;

/** 연동 문서 §2.8 POST /problems/{id}/open 응답 (OpenProblemResponse). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenProblemResponseDTO {
    private String solveSessionId;
    private String openedAt; // ISO 8601 (+09:00)
    private ProblemDetailResponseDTO problem;
    private ProblemBodyResponseDTO body;

    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    public static OpenProblemResponseDTO of(OpenProblemDTO open) {
        return new OpenProblemResponseDTO(
                open.getSolveSessionId(),
                open.getOpenedAt().atOffset(KST).toString(),
                ProblemDetailResponseDTO.of(open.getProblem()),
                ProblemBodyResponseDTO.of(open.getBody())
        );
    }
}
