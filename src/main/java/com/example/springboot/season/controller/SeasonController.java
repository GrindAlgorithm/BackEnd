package com.example.springboot.season.controller;

import com.example.springboot.problem.dto.ProblemSummaryResponseDTO;
import com.example.springboot.season.dto.SeasonDTO;
import com.example.springboot.season.dto.SeasonDetailDTO;
import com.example.springboot.season.dto.SeasonDetailResponseDTO;
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

    /**
     * GET /api/v1/seasons/current — 시즌 화면 통합 응답 (연동 문서 §2.5-c)
     * 기간 진행률 + 시즌 문제(내 진행 상태 포함) + 리워드 달성 현황 + 지난 시즌 챔피언.
     * ("current" 리터럴 매핑이 /{seasonId} 보다 우선 매칭된다)
     */
    @GetMapping("/current")
    public ResponseResult<SeasonDetailResponseDTO> getCurrentSeasonDetail() {
        SeasonDetailDTO detail = seasonService.getCurrentSeasonDetail();
        if (detail == null) {
            return ResponseResult.<SeasonDetailResponseDTO>error(null);
        }

        if (log.isInfoEnabled()) {
            log.info("getCurrentSeasonDetail Controller Success : seasonId={}, {} problems, {} rewards",
                    detail.getSeason().getId(), detail.getProblems().size(), detail.getRewards().size());
        }
        return ResponseResult.success(SeasonDetailResponseDTO.of(detail));
    }

    /** GET /api/v1/seasons/{seasonId} — 시즌 단건 기간 정보 (문제 탭 시즌 정보) */
    @GetMapping("/{seasonId}")
    public ResponseResult<SeasonResponseDTO> getSeason(@PathVariable Integer seasonId) {
        SeasonDTO season = seasonService.getSeason(seasonId);
        if (season == null) {
            return ResponseResult.<SeasonResponseDTO>error(null);
        }

        if (log.isInfoEnabled()) {
            log.info("getSeason Controller Success : seasonId={}", seasonId);
        }
        return ResponseResult.success(SeasonResponseDTO.of(season));
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
