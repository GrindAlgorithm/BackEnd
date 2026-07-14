package com.example.springboot.ranking.controller;

import com.example.springboot.ranking.dto.RankingResponseDTO;
import com.example.springboot.ranking.dto.RankingScope;
import com.example.springboot.ranking.service.RankingService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
@Slf4j
public class RankingController {

    private final RankingService rankingService;

    /**
     * GET /api/v1/rankings?scope={season|overall|friends} — 랭킹 탭 (연동 문서 §2.13)
     * season(기본): 현재 시즌 점수 순 / overall: 전체 누적 / friends: 친구(미구현, 내 순위만).
     * entries 는 순위 순, myEntry 는 목록에 없어도 채운다.
     */
    @GetMapping("")
    public ResponseResult<RankingResponseDTO> getRanking(
            @RequestParam(required = false, defaultValue = "season") String scope) {

        RankingResponseDTO ranking = RankingResponseDTO.of(
                rankingService.getRanking(RankingScope.fromValue(scope)));

        if (log.isInfoEnabled()) {
            log.info("getRanking Controller Success : scope={}, {} entries", scope, ranking.getEntries().size());
        }
        return ResponseResult.success(ranking);
    }
}
