package com.example.springboot.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 잔디 한 칸 — 연동 문서 §2.4 seasonActivity.days[] (ActivityDay) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDayDTO {
    private String date; // YYYY-MM-DD
    private int count;   // 그날 푼 문제 수
    private int level;   // 0~4 잔디 강도
}
