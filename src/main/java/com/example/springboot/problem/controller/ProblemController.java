package com.example.springboot.problem.controller;

import com.example.springboot.problem.dto.ProblemDetailDTO;
import com.example.springboot.problem.dto.ProblemDetailResponseDTO;
import com.example.springboot.problem.service.ProblemService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
@Slf4j
public class ProblemController {

    private final ProblemService problemService;

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
}
