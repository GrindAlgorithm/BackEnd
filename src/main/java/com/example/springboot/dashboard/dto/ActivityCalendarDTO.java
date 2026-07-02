package com.example.springboot.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 이번 시즌 활동(잔디) — 연동 문서 §2.4 dashboard.seasonActivity (ActivityCalendar) */
@Getter
@AllArgsConstructor
public class ActivityCalendarDTO {

    /** 최대 12주(84일) */
    private static final int MAX_DAYS = 84;

    private List<ActivityDayDTO> days; // 과거 → 오늘 순
    private int activeDays;
    private double avgPerDay;

    /**
     * 시즌 시작일부터 오늘까지의 빈 잔디 달력(최대 12주)을 만든다.
     * 실제 활동 수치는 제출/활동 도메인 연동 시 채운다 — 현재는 count/level 0.
     */
    public static ActivityCalendarDTO emptyFrom(LocalDate start, LocalDate today) {
        LocalDate windowStart = today.minusDays(MAX_DAYS - 1L);
        LocalDate from = start.isBefore(windowStart) ? windowStart : start;
        if (from.isAfter(today)) {
            from = today;
        }

        List<ActivityDayDTO> days = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(today); d = d.plusDays(1)) {
            days.add(new ActivityDayDTO(d.toString(), 0, 0));
        }
        return new ActivityCalendarDTO(days, 0, 0.0);
    }
}
