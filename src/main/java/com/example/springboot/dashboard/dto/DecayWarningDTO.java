package com.example.springboot.dashboard.dto;

import com.example.springboot.problem.dto.TierRankDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 하락 경고 — 연동 문서 §2.4 dashboard.decay.
 * ⚠ 하락 공식/트리거(A1)는 미확정(추후 개발). 구현 전까지 dashboard.decay 는 null 로 내려간다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecayWarningDTO {
    private int inactiveDays;
    private int daysUntilDrop;
    private TierRankDTO fromTier;
    private TierRankDTO toTier;
}
