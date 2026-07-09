package com.example.springboot.problem.dto;

import com.example.springboot.problem.entity.ProblemSampleEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 본문 DTO 매핑 검증 (Spring/DB 불필요) — 연동 문서 §2.8 */
class ProblemBodyDTOTest {

    @Test
    void mapsBodyAndSamples() {
        ProblemSampleEntity s1 = ProblemSampleEntity.createProblemSampleEntity(null, 1, "3\n1 1\n4 5\n2 3", "14");

        // body 엔티티 없이도 샘플은 매핑되고, 본문 필드는 null (본문 미등록 방어)
        ProblemBodyDTO nullBody = ProblemBodyDTO.of(null, List.of(s1));
        assertNull(nullBody.getDescription());
        assertEquals(1, nullBody.getSamples().size());
        assertEquals("3\n1 1\n4 5\n2 3", nullBody.getSamples().get(0).getInput());
        assertEquals("14", nullBody.getSamples().get(0).getOutput());
    }
}
