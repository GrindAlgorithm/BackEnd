package com.example.springboot.user.dto;

import com.example.springboot.submission.entity.SubmissionEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 최근 제출 — 연동 문서 §2.15 recentSubmissions[] (RecentSubmissionItem) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecentSubmissionItemDTO {
    private Long submissionId;
    private String problemId;
    private String displayNo;
    private String title;
    private String status; // SubmissionStatus snake_case 값 (§1.6)
    private LocalDateTime submittedAt;

    public static RecentSubmissionItemDTO of(SubmissionEntity submission) {
        return new RecentSubmissionItemDTO(
                submission.getId(),
                submission.getProblem().getProblemId(),
                submission.getProblem().getDisplayNo(),
                submission.getProblem().getTitle(),
                submission.getStatus().getValue(),
                submission.getSubmittedAt()
        );
    }
}
