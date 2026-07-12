package com.example.springboot.submission.dto;

import com.example.springboot.judge0.Judge0Execution;
import com.example.springboot.judge0.Judge0Verdict;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §2.9 POST /runs 응답 (RunResult). status: ok | compile_error | runtime_error | time_limit */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RunResultResponseDTO {
    private String status;
    private String stdout;
    private String stderr;
    private Long timeMs;
    private Long memoryKb;
    private Integer exitCode;

    public static RunResultResponseDTO of(Judge0Execution exec) {
        return new RunResultResponseDTO(
                runStatus(exec.verdict()),
                exec.stdout(),
                exec.stderr(),
                exec.timeMs(),
                exec.memoryKb(),
                exec.exitCode()
        );
    }

    /** 실행에는 오답 개념이 없어 ACCEPTED=ok, 나머지 오류는 계약의 4상태로 축약 */
    private static String runStatus(Judge0Verdict verdict) {
        return switch (verdict) {
            case ACCEPTED -> "ok";
            case COMPILE_ERROR -> "compile_error";
            case TIME_LIMIT -> "time_limit";
            default -> "runtime_error";
        };
    }
}
