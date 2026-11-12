package com.example.springboot.ranking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 현재 시즌 순위 추이 — 연동 문서 §2.15 rankHistory.
 * points 는 과거→오늘 순. 마지막 점은 오늘 실시간 순위(스냅샷이 아니라 조회 시점 값).
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RankHistoryDTO {

    private Integer seasonId;
    private String seasonName;
    private List<Point> points;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private int rank;
        private int score;
    }
}
