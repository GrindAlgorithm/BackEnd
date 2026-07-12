package com.example.springboot.judge0;

/** Judge0 단건 실행 결과(정규화). */
public record Judge0Execution(
        Judge0Verdict verdict,
        String stdout,      // nullable
        String stderr,      // nullable (compile_output 포함)
        Long timeMs,        // nullable
        Long memoryKb,      // nullable
        Integer exitCode    // nullable
) {
}
