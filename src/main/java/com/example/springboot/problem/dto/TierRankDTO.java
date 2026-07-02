package com.example.springboot.problem.dto;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §1.6 TierRank = { name, level } */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TierRankDTO {
    private String name;  // "gold"
    private String level; // "IV"

    public static TierRankDTO of(TierName name, TierLevel level) {
        return new TierRankDTO(name.getValue(), level.name());
    }
}
