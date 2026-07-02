package com.example.springboot.dashboard.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** 잔디 달력 생성 로직 검증 (Spring/DB 불필요) */
class ActivityCalendarDTOTest {

    @Test
    void generatesInclusiveRangeFromStartToToday() {
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate today = LocalDate.of(2026, 7, 3);

        ActivityCalendarDTO cal = ActivityCalendarDTO.emptyFrom(start, today);

        assertEquals(3, cal.getDays().size()); // 7/1, 7/2, 7/3
        assertEquals("2026-07-01", cal.getDays().get(0).getDate());
        assertEquals("2026-07-03", cal.getDays().get(2).getDate());
        assertEquals(0, cal.getActiveDays());
    }

    @Test
    void capsWindowAt12Weeks() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate today = LocalDate.of(2026, 7, 1); // 시작이 12주보다 오래됨

        ActivityCalendarDTO cal = ActivityCalendarDTO.emptyFrom(start, today);

        assertEquals(84, cal.getDays().size()); // 최대 12주(84일)
    }
}
