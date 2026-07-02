package com.example.springboot.season.controller;

import com.example.springboot.problem.dto.ProblemSummaryResponseDTO;
import com.example.springboot.season.dto.SeasonResponseDTO;
import com.example.springboot.season.service.SeasonService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
@Slf4j
public class SeasonController {

    private final SeasonService seasonService;

    /** GET /api/v1/seasons — 시즌 목록 (연동 문서 §2.5) */
    @GetMapping("")
    public ResponseResult<List<SeasonResponseDTO>> getSeasons() {
        List<SeasonResponseDTO> seasons = seasonService.getSeasons().stream()
                .map(SeasonResponseDTO::of)
                .toList();

        if (log.isInfoEnabled()) {
            log.info("getSeasons Controller Success : {} seasons", seasons.size());
        }
        return ResponseResult.success(seasons);
    }

    /** GET /api/v1/seasons/{seasonId}/problems — 시즌 문제 목록 (연동 문서 §2.6) */
    @GetMapping("/{seasonId}/problems")
    public ResponseResult<List<ProblemSummaryResponseDTO>> getSeasonProblems(@PathVariable Integer seasonId) {
        List<ProblemSummaryResponseDTO> problems = seasonService.getSeasonProblems(seasonId).stream()
                .map(ProblemSummaryResponseDTO::of)
                .toList();

        if (log.isInfoEnabled()) {
            log.info("getSeasonProblems Controller Success : seasonId={}, {} problems", seasonId, problems.size());
        }
        return ResponseResult.success(problems);
    }
}
