package com.example.springboot.dashboard.dto;

import com.example.springboot.common.activity.ActivityStats;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 이번 시즌 활동(잔디) — 연동 문서 §2.4 dashboard.seasonActivity (ActivityCalendar) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCalendarDTO {

    /** 최대 12주(84일) */
    private static final int MAX_DAYS = 84;

    private List<ActivityDayDTO> days; // 과거 → 오늘 순
    private int activeDays;
    private double avgPerDay;

    /** 시즌 시작일부터 오늘까지의 빈 잔디 달력(최대 12주) — 비로그인 등 활동 데이터가 없을 때 */
    public static ActivityCalendarDTO emptyFrom(LocalDate start, LocalDate today) {
        return of(start, today, Map.of());
    }

    /**
     * 시즌 시작일부터 오늘까지의 잔디 달력(최대 12주).
     *
     * @param countByDate 날짜별 그날 처음 해결한 문제 수 (창 밖 날짜는 무시)
     */
    public static ActivityCalendarDTO of(LocalDate start, LocalDate today, Map<LocalDate, Integer> countByDate) {
        LocalDate windowStart = today.minusDays(MAX_DAYS - 1L);
        LocalDate from = start.isBefore(windowStart) ? windowStart : start;
        if (from.isAfter(today)) {
            from = today;
        }

        List<ActivityDayDTO> days = new ArrayList<>();
        int activeDays = 0;
        int total = 0;
        for (LocalDate d = from; !d.isAfter(today); d = d.plusDays(1)) {
            int count = countByDate.getOrDefault(d, 0);
            days.add(new ActivityDayDTO(d.toString(), count, ActivityStats.jandiLevel(count)));
            if (count > 0) {
                activeDays++;
                total += count;
            }
        }
        double avgPerDay = activeDays == 0 ? 0.0 : Math.round(total * 10.0 / activeDays) / 10.0;
        return new ActivityCalendarDTO(days, activeDays, avgPerDay);
    }
}
