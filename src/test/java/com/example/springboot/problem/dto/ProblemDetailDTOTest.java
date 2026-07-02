package com.example.springboot.problem.dto;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.entity.ProblemStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 문제 상세 계산 로직 검증 (Spring/DB 불필요) — 연동 문서 §2.7 */
class ProblemDetailDTOTest {

    @Test
    void acceptanceRateRoundsToOneDecimal() {
        ProblemEntity problem = ProblemEntity.createProblemEntity(
                "S2-08", "S2-08", "그래프 가중치 처리", TierName.GOLD, TierLevel.IV, null, List.of("다익스트라"),
                1, 512, "O(N^4) 이내", 8432, 2791, 2113, 234);

        assertEquals(33.1, problem.acceptanceRate());
    }

    @Test
    void nonSeasonalProblemHasNullPointsAndSeasonId() {
        ProblemEntity problem = ProblemEntity.createProblemEntity(
                "21609", "21609", "상어 중학교", TierName.PLATINUM, TierLevel.V, null, List.of("시뮬레이션"),
                1, 256, "O(N^2)", 100, 40, 38, 5);

        ProblemDetailDTO dto = ProblemDetailDTO.of(problem);

        assertNull(dto.getPoints());
        assertNull(dto.getSeasonId());
        assertEquals(40.0, dto.getAcceptanceRate());
        assertEquals(ProblemStatus.UNTRIED, dto.getMyStatus());
        assertNull(dto.getLastTriedAt());
    }
}
