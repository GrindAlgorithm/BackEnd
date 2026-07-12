package com.example.springboot.judge0;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 로컬 시뮬레이션 클라이언트 (기본). 실제 Judge0 없이 채점 파이프라인을 동작시킨다.
 * - 채점(expectedOutput 있음): 기대 출력을 그대로 stdout으로 반환 → ACCEPTED (정답 처리)
 * - 실행(expectedOutput 없음): stdin 을 echo → ACCEPTED(=실행 성공)
 * ⚠ 실제 코드를 실행하지 않으므로 오답/런타임에러를 판별하지 못한다.
 *   진짜 채점은 judge0.client=real + 실제 Judge0 필요.
 */
@Component
@ConditionalOnProperty(name = "judge0.client", havingValue = "stub", matchIfMissing = true)
@Slf4j
public class StubJudge0Client implements Judge0Client {

    @Override
    public Judge0Execution execute(Judge0ExecRequest request) {
        boolean grading = request.expectedOutput() != null;
        if (log.isInfoEnabled()) {
            log.info("[judge0-stub] execute languageId={} grading={} (실제 실행 아님)",
                    request.languageId(), grading);
        }
        String stdout = grading ? request.expectedOutput() : nullToEmpty(request.stdin());
        return new Judge0Execution(Judge0Verdict.ACCEPTED, stdout, null, 42L, 15000L, 0);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
