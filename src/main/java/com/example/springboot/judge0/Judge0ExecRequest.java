package com.example.springboot.judge0;

/**
 * Judge0 단건 실행 요청.
 * expectedOutput 이 있으면 Judge0가 출력 비교까지 수행(제출 채점), 없으면 실행만(코드 실행).
 */
public record Judge0ExecRequest(
        int languageId,
        String sourceCode,
        String stdin,          // nullable
        String expectedOutput, // nullable
        float cpuTimeLimitSec,
        int memoryLimitKb
) {
}
