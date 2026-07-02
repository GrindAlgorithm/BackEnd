package com.example.springboot.season.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §2.5 GET /seasons 응답 항목 (SeasonSummary) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonResponseDTO {
    private Integer id;
    private String name;
    private String startDate; // YYYY-MM-DD
    private String endDate;

    /** "current" | "past" | "beta" */
    private String status;

    /** 현재 시즌일 때만 종료까지 남은 일수, 그 외 null */
    @JsonProperty("dDay")
    private Long dDay;

    public static SeasonResponseDTO of(SeasonDTO season) {
        return new SeasonResponseDTO(
                season.getId(),
                season.getName(),
                season.getStartDate().toString(),
                season.getEndDate().toString(),
                season.getStatus().getValue(),
                season.getDDay()
        );
    }
}
