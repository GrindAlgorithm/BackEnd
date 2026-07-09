package com.example.springboot.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** 연동 문서 §2.8 body = { description, inputSpec, outputSpec, samples[] } */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemBodyResponseDTO {
    private String description;
    private String inputSpec;
    private String outputSpec;
    private List<ProblemSampleDTO> samples;

    public static ProblemBodyResponseDTO of(ProblemBodyDTO body) {
        return new ProblemBodyResponseDTO(
                body.getDescription(),
                body.getInputSpec(),
                body.getOutputSpec(),
                body.getSamples()
        );
    }
}
