package com.example.springboot.submission.service;

import com.example.springboot.judge0.Judge0Client;
import com.example.springboot.judge0.Judge0ExecRequest;
import com.example.springboot.judge0.Judge0Execution;
import com.example.springboot.judge0.Judge0Verdict;
import com.example.springboot.problem.repository.ProblemSampleRepository;
import com.example.springboot.problem.repository.ProblemTestcaseRepository;
import com.example.springboot.submission.entity.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 비동기 채점 오케스트레이터 (연동 문서 §2.11 폴링 대응).
 * problem_testcase(히든 포함)를 순회하며 Judge0로 채점하고,
 * 케이스마다 진행률을 독립 트랜잭션으로 커밋한다. 첫 실패 케이스에서 종결.
 * 테스트케이스가 없는 문제는 공개 예제(problem_sample)로 폴백한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubmissionGrader {

    /** 채점 입출력 쌍 — testcase/sample 공통 형태 */
    record TestCase(String input, String output) {
    }

    private final Judge0Client judge0Client;
    private final ProblemTestcaseRepository problemTestcaseRepository;
    private final ProblemSampleRepository problemSampleRepository;
    private final SubmissionGradingTx tx;

    @Async
    public void grade(long submissionId, String problemId, int judge0LangId,
                      int cpuTimeLimitSec, int memoryLimitKb, String sourceCode) {
        long maxTimeMs = 0;
        long maxMemoryKb = 0;
        try {
            tx.markJudging(submissionId);

            List<TestCase> tests = loadTestCases(problemId);
            if (tests.isEmpty()) {
                // 채점 근거가 전혀 없는 문제 — 콘텐츠 누락. 정답 처리하되 명확히 남긴다
                log.warn("grade: 테스트케이스·예제 모두 없음 — 무검증 ACCEPTED problemId={} submissionId={}",
                        problemId, submissionId);
                tx.finishAccepted(submissionId, 0L, 0L);
                return;
            }

            int total = tests.size();
            int done = 0;

            for (TestCase test : tests) {
                Judge0Execution exec = judge0Client.execute(new Judge0ExecRequest(
                        judge0LangId, sourceCode, test.input(), test.output(),
                        cpuTimeLimitSec, memoryLimitKb));
                done++;
                if (exec.timeMs() != null) {
                    maxTimeMs = Math.max(maxTimeMs, exec.timeMs());
                }
                if (exec.memoryKb() != null) {
                    maxMemoryKb = Math.max(maxMemoryKb, exec.memoryKb());
                }

                if (exec.verdict() != Judge0Verdict.ACCEPTED) {
                    tx.finishFailed(submissionId, toStatus(exec.verdict()), maxTimeMs, maxMemoryKb);
                    if (log.isInfoEnabled()) {
                        log.info("grade done submissionId={} verdict={} at case {}/{}",
                                submissionId, exec.verdict(), done, total);
                    }
                    return;
                }
                tx.updateProgress(submissionId, (int) (done * 100L / total), maxTimeMs, maxMemoryKb);
            }

            tx.finishAccepted(submissionId, maxTimeMs, maxMemoryKb);
            if (log.isInfoEnabled()) {
                log.info("grade done submissionId={} ACCEPTED ({} cases)", submissionId, total);
            }
        } catch (Exception e) {
            // Judge0 통신/한도 초과(422) 등 예외 시 제출을 JUDGING 에 방치하지 않고 종결한다.
            log.error("grade failed submissionId={} — 채점 오류로 RUNTIME_ERROR 종결", submissionId, e);
            try {
                tx.finishFailed(submissionId, SubmissionStatus.RUNTIME_ERROR, maxTimeMs, maxMemoryKb);
            } catch (Exception ex) {
                log.error("grade 실패 종결마저 실패 submissionId={}", submissionId, ex);
            }
        }
    }

    /** 채점 케이스 로드 — problem_testcase 우선(히든 포함), 없으면 공개 예제 폴백 */
    private List<TestCase> loadTestCases(String problemId) {
        List<TestCase> cases = problemTestcaseRepository.findByProblem_ProblemIdOrderByOrdinalAsc(problemId).stream()
                .map(t -> new TestCase(t.getInput(), t.getOutput()))
                .toList();
        if (!cases.isEmpty()) {
            return cases;
        }
        return problemSampleRepository.findByProblem_ProblemIdOrderByOrdinalAsc(problemId).stream()
                .map(s -> new TestCase(s.getInput(), s.getOutput()))
                .toList();
    }

    private SubmissionStatus toStatus(Judge0Verdict verdict) {
        return switch (verdict) {
            case ACCEPTED -> SubmissionStatus.ACCEPTED;
            case WRONG_ANSWER -> SubmissionStatus.WRONG_ANSWER;
            case TIME_LIMIT -> SubmissionStatus.TIME_LIMIT;
            case MEMORY_LIMIT -> SubmissionStatus.MEMORY_LIMIT;
            case COMPILE_ERROR -> SubmissionStatus.COMPILE_ERROR;
            default -> SubmissionStatus.RUNTIME_ERROR; // RUNTIME_ERROR, INTERNAL_ERROR
        };
    }
}
