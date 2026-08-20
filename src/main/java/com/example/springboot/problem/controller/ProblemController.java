package com.example.springboot.problem.controller;

import com.example.springboot.problem.dto.OpenProblemDTO;
import com.example.springboot.problem.dto.OpenProblemResponseDTO;
import com.example.springboot.problem.dto.ProblemDetailDTO;
import com.example.springboot.problem.dto.ProblemDetailResponseDTO;
import com.example.springboot.problem.service.ProblemService;
import com.example.springboot.user.CurrentUserProvider;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
@Slf4j
public class ProblemController {

    // 미로그인 폴백 — /problems/** 는 permitAll 이라 익명 열람이 올 수 있다
    private static final String ANONYMOUS = "anonymous";

    private final ProblemService problemService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * GET /api/v1/problems/{problemId} — 문제 상세 (연동 문서 §2.7)
     * 노출: 시간/메모리 제한, 제출/정답/맞힌 사람/정답 비율, 시즌 점수, 알고리즘 분류 등 메타.
     * ⚠ 부정행위 방지(B2): 본문·예제는 포함하지 않는다 (본문은 POST /problems/{id}/open 전용).
     */
    @GetMapping("/{problemId}")
    public ResponseResult<ProblemDetailResponseDTO> getProblem(@PathVariable String problemId) {
        ProblemDetailDTO problem = problemService.getProblem(problemId);
        if (problem == null) {
            return ResponseResult.<ProblemDetailResponseDTO>error(null);
        }

        if (log.isInfoEnabled()) {
            log.info("getProblem Controller Success : problemId={}", problemId);
        }
        return ResponseResult.success(ProblemDetailResponseDTO.of(problem));
    }

    /**
     * POST /api/v1/problems/{problemId}/open — IDE 진입 = 본문 열람 (연동 문서 §2.8)
     * 본문(설명/입력/출력/예제)을 내려주고, 풀이 세션 발급 + 열람 시각(풀이 시작 시각)을 기록한다(B2).
     */
    @PostMapping("/{problemId}/open")
    public ResponseResult<OpenProblemResponseDTO> openProblem(@PathVariable String problemId) {
        // 풀이 세션은 로그인 유저 handle 로 기록 — 이후 제출·무결성 신호(§2.17)와 조인된다
        String handle = currentUserProvider.currentHandle();
        OpenProblemDTO open = problemService.openProblem(problemId, handle != null ? handle : ANONYMOUS);
        if (open == null) {
            return ResponseResult.<OpenProblemResponseDTO>error(null);
        }

        if (log.isInfoEnabled()) {
            log.info("openProblem Controller Success : problemId={}, solveSessionId={}",
                    problemId, open.getSolveSessionId());
        }
        return ResponseResult.success(OpenProblemResponseDTO.of(open));
    }
}
