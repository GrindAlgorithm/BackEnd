package com.example.springboot.submission.controller;

import com.example.springboot.judge0.Judge0Execution;
import com.example.springboot.submission.dto.RunRequestDTO;
import com.example.springboot.submission.dto.RunResultResponseDTO;
import com.example.springboot.submission.service.RunService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/runs")
@RequiredArgsConstructor
@Slf4j
public class RunController {

    private final RunService runService;

    /**
     * POST /api/v1/runs — 코드 실행 (예제 테스트용, 채점 아님) (연동 문서 §2.9)
     * Judge0로 동기 실행하고 stdout/stderr/시간/메모리를 반환한다. 점수/기록 없음.
     */
    @PostMapping("")
    public ResponseResult<RunResultResponseDTO> run(@RequestBody RunRequestDTO request) {
        Judge0Execution exec = runService.run(request);
        if (exec == null) {
            return ResponseResult.<RunResultResponseDTO>error(null);
        }

        if (log.isInfoEnabled()) {
            log.info("run Controller Success : problemId={}, verdict={}", request.getProblemId(), exec.verdict());
        }
        return ResponseResult.success(RunResultResponseDTO.of(exec));
    }
}
