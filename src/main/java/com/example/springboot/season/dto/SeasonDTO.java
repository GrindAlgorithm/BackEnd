package com.example.springboot.season.dto;

import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** 시즌 서비스 모델 (연동 문서 §2.5). 표현 변환은 {@link SeasonResponseDTO} 에서 수행. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonDTO {
    private Integer id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private SeasonStatus status;
    private Long dDay; // 현재 시즌일 때만, 그 외 null

    public static SeasonDTO of(SeasonEntity season, LocalDate today) {
        Long dDay = season.getStatus() == SeasonStatus.CURRENT
                ? ChronoUnit.DAYS.between(today, season.getEndDate())
                : null;
        return new SeasonDTO(
                season.getId(),
                season.getName(),
                season.getStartDate(),
                season.getEndDate(),
                season.getStatus(),
                dDay
        );
    }
}
