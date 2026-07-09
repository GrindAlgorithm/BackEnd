package com.example.springboot.problem.dto;

import com.example.springboot.problem.entity.ProblemBodyEntity;
import com.example.springboot.problem.entity.ProblemSampleEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** 문제 본문 서비스 모델 (연동 문서 §2.8 body). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemBodyDTO {
    private String description;
    private String inputSpec;
    private String outputSpec;
    private List<ProblemSampleDTO> samples;

    public static ProblemBodyDTO of(ProblemBodyEntity body, List<ProblemSampleEntity> samples) {
        List<ProblemSampleDTO> sampleDTOs = samples.stream().map(ProblemSampleDTO::of).toList();
        if (body == null) {
            // 본문 미등록 문제 방어 — 빈 본문 반환
            return new ProblemBodyDTO(null, null, null, sampleDTOs);
        }
        return new ProblemBodyDTO(body.getDescription(), body.getInputSpec(), body.getOutputSpec(), sampleDTOs);
    }
}
