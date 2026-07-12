package com.example.springboot.judge0;

import com.example.springboot.submission.dto.RunResultResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Judge0 status → verdict → run status 매핑 검증 (Spring/DB 불필요) — 연동 문서 §2.9/§3 */
class Judge0VerdictTest {

    @Test
    void mapsJudge0StatusId() {
        assertEquals(Judge0Verdict.ACCEPTED, Judge0Verdict.fromStatusId(3));
        assertEquals(Judge0Verdict.WRONG_ANSWER, Judge0Verdict.fromStatusId(4));
        assertEquals(Judge0Verdict.TIME_LIMIT, Judge0Verdict.fromStatusId(5));
        assertEquals(Judge0Verdict.COMPILE_ERROR, Judge0Verdict.fromStatusId(6));
        assertEquals(Judge0Verdict.RUNTIME_ERROR, Judge0Verdict.fromStatusId(11));
        assertEquals(Judge0Verdict.INTERNAL_ERROR, Judge0Verdict.fromStatusId(13));
    }

    @Test
    void runStatusCollapsesToContractStates() {
        // 실행(§2.9)에는 오답 개념이 없어 4상태로 축약된다
        assertEquals("ok", RunResultResponseDTO.of(exec(Judge0Verdict.ACCEPTED)).getStatus());
        assertEquals("compile_error", RunResultResponseDTO.of(exec(Judge0Verdict.COMPILE_ERROR)).getStatus());
        assertEquals("time_limit", RunResultResponseDTO.of(exec(Judge0Verdict.TIME_LIMIT)).getStatus());
        assertEquals("runtime_error", RunResultResponseDTO.of(exec(Judge0Verdict.WRONG_ANSWER)).getStatus());
    }

    private Judge0Execution exec(Judge0Verdict verdict) {
        return new Judge0Execution(verdict, "out", null, 10L, 100L, 0);
    }
}
