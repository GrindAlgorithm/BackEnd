package com.example.springboot.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 연동 문서 §2.11 submission.user = { handle } */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionUserDTO {
    private String handle;
}
