package com.example.springboot.submission.controller;

import com.example.springboot.submission.dto.SubmissionResponseDTO;
import com.example.springboot.submission.service.SubmissionService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
@Slf4j
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * GET /api/v1/submissions — 채점 현황 목록 (연동 문서 §2.12)
     * 문제 탭의 "채점 현황" 내부 탭: seasonId 로 시즌별 조회, problemId/mine 로 추가 필터.
     */
    @GetMapping("")
    public ResponseResult<List<SubmissionResponseDTO>> getSubmissions(
            @RequestParam(required = false) Integer seasonId,
            @RequestParam(required = false) String problemId,
            @RequestParam(required = false, defaultValue = "false") boolean mine) {

        List<SubmissionResponseDTO> submissions = submissionService.getSubmissions(seasonId, problemId, mine).stream()
                .map(SubmissionResponseDTO::of)
                .toList();

        if (log.isInfoEnabled()) {
            log.info("getSubmissions Controller Success : seasonId={}, problemId={}, {} submissions",
                    seasonId, problemId, submissions.size());
        }
        return ResponseResult.success(submissions);
    }
}
