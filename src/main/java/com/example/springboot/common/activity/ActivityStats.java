package com.example.springboot.common.activity;

import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 제출 이력 기반 활동 지표 — 프로필(§2.15)과 대시보드(§2.4)가 같은 정의를 쓰도록 한곳에 둔다. */
public final class ActivityStats {

    private ActivityStats() {
    }

    /** 문제별 최초 정답 시각 (입력 순서 유지). 푼 문제 수·잔디 카운트의 공통 기준 */
    public static Map<ProblemEntity, LocalDateTime> firstSolvedAt(List<SubmissionEntity> submissions) {
        Map<ProblemEntity, LocalDateTime> first = new LinkedHashMap<>();
        for (SubmissionEntity s : submissions) {
            if (s.getStatus() == SubmissionStatus.ACCEPTED) {
                first.merge(s.getProblem(), s.getSubmittedAt(), (a, b) -> a.isBefore(b) ? a : b);
            }
        }
        return first;
    }

    /** 현재 연속 활동 일수 — 오늘 활동이 아직 없으면 어제까지 이어진 것으로 본다 */
    public static int currentStreak(Set<LocalDate> activeDates, LocalDate today) {
        LocalDate cursor = activeDates.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (activeDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /** 최장 연속 활동 일수 — sortedDates 는 오름차순이어야 한다 */
    public static int longestStreak(Iterable<LocalDate> sortedDates) {
        int longest = 0;
        int run = 0;
        LocalDate prev = null;
        for (LocalDate d : sortedDates) {
            run = (prev != null && prev.plusDays(1).equals(d)) ? run + 1 : 1;
            longest = Math.max(longest, run);
            prev = d;
        }
        return longest;
    }

    /** 잔디 강도 0~4 (그날 처음 해결한 문제 수 기준) */
    public static int jandiLevel(int count) {
        if (count <= 0) return 0;
        if (count == 1) return 1;
        if (count == 2) return 2;
        if (count <= 4) return 3;
        return 4;
    }

    /** 소수 첫째 자리 반올림 백분율. 분모 0 이면 0 */
    public static double percent(long part, long whole) {
        return whole == 0 ? 0.0 : Math.round(part * 1000.0 / whole) / 10.0;
    }
}
