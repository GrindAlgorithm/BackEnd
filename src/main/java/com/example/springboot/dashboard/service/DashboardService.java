package com.example.springboot.dashboard.service;

import com.example.springboot.dashboard.dto.DashboardDTO;

public interface DashboardService {

    /** 홈 화면 통합 데이터 — 연동 문서 §2.4 */
    public DashboardDTO getDashboard();
}
