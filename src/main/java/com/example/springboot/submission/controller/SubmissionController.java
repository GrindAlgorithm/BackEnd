package com.example.springboot.submission.controller;

import com.example.springboot.submission.dto.SubmissionDTO;
import com.example.springboot.submission.dto.SubmissionResponseDTO;
import com.example.springboot.submission.dto.SubmitRequestDTO;
import com.example.springboot.submission.dto.SubmitResponseDTO;
import com.example.springboot.submission.service.SubmissionService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
@Slf4j
public class SubmissionController {

    // 인증 연동 전 임시 유저
    private static final String ANONYMOUS = "anonymous";

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

    /**
     * POST /api/v1/submissions — 제출 = 비동기 채점 시작 (연동 문서 §2.10)
     * 즉시 submissionId 를 반환하고, 채점은 백그라운드로 진행된다(GET 폴링).
     */
    @PostMapping("")
    public ResponseResult<SubmitResponseDTO> submit(@RequestBody SubmitRequestDTO request) {
        Long submissionId = submissionService.submit(request, ANONYMOUS);
        if (submissionId == null) {
            return ResponseResult.<SubmitResponseDTO>error(null);
        }

        if (log.isInfoEnabled()) {
            log.info("submit Controller Success : problemId={}, submissionId={}",
                    request.getProblemId(), submissionId);
        }
        return ResponseResult.success(new SubmitResponseDTO(submissionId));
    }

    /**
     * GET /api/v1/submissions/{submissionId} — 채점 상태 (폴링) (연동 문서 §2.11)
     * 종결 상태가 나올 때까지 프론트가 ~1초 간격으로 폴링한다.
     */
    @GetMapping("/{submissionId}")
    public ResponseResult<SubmissionResponseDTO> getSubmission(@PathVariable long submissionId) {
        SubmissionDTO submission = submissionService.getSubmission(submissionId);
        if (submission == null) {
            return ResponseResult.<SubmissionResponseDTO>error(null);
        }
        return ResponseResult.success(SubmissionResponseDTO.of(submission));
    }
}
