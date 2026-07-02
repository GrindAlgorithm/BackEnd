package com.example.springboot.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §2.11 submission.problem = { problemId, displayNo, title } */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionProblemDTO {
    private String problemId;
    private String displayNo;
    private String title;
}
