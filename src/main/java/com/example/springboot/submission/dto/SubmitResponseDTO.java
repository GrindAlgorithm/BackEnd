package com.example.springboot.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §2.10 POST /submissions 응답 = { submissionId } */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitResponseDTO {
    private long submissionId;
}
