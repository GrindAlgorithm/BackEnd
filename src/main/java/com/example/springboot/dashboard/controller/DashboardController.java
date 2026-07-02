package com.example.springboot.dashboard.controller;

import com.example.springboot.dashboard.dto.DashboardResponseDTO;
import com.example.springboot.dashboard.service.DashboardService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/v1/dashboard — 홈 화면 통합 응답 (연동 문서 §2.4)
     * 오늘의 추천 / 내 주변 순위 / 시즌 정보 / 이번주 통계 / 공지 / 이번 시즌 활동을 한 번에 반환한다.
     */
    @GetMapping("")
    public ResponseResult<DashboardResponseDTO> getDashboard() {
        DashboardResponseDTO dashboard = dashboardService.getDashboard();

        if (log.isInfoEnabled()) {
            log.info("getDashboard Controller Success : todayPicks={}, notices={}",
                    dashboard.getTodayPicks().size(), dashboard.getNotices().size());
        }
        return ResponseResult.success(dashboard);
    }
}
