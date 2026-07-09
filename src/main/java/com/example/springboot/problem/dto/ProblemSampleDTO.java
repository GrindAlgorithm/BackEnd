package com.example.springboot.problem.dto;

import com.example.springboot.problem.entity.ProblemSampleEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §2.8 body.samples[] = { input, output }. 변환이 없어 서비스/응답 공용. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSampleDTO {
    private String input;
    private String output;

    public static ProblemSampleDTO of(ProblemSampleEntity sample) {
        return new ProblemSampleDTO(sample.getInput(), sample.getOutput());
    }
}
