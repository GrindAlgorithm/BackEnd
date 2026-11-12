package com.example.springboot.dashboard.dto;

import com.example.springboot.problem.dto.TierRankDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 하락 경고 — 연동 문서 §2.4 dashboard.decay.
 * 규칙은 common.tier.TierDecay (7일 비활동마다 1단계, 골드 IV 이상, 하한 골드 V). 곧 하락할 때만 내려가고 그 외 null.
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
