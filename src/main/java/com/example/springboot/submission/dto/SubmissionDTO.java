package com.example.springboot.submission.dto;

import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.submission.entity.LanguageCode;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 채점 현황 서비스 모델 (연동 문서 §2.12). 표현 변환은 {@link SubmissionResponseDTO} 에서 수행. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO {
    private long submissionId;
    private String userHandle;
    private String problemId;
    private String displayNo;
    private String title;
    private SubmissionStatus status;
    private Integer progress;
    private Long timeMs;
    private Long memoryKb;
    private LanguageCode language;
    private int codeBytes;
    private LocalDateTime submittedAt;

    public static SubmissionDTO of(SubmissionEntity submission) {
        ProblemEntity problem = submission.getProblem();
        return new SubmissionDTO(
                submission.getId(),
                submission.getUserHandle(),
                problem.getProblemId(),
                problem.getDisplayNo(),
                problem.getTitle(),
                submission.getStatus(),
                submission.getProgress(),
                submission.getTimeMs(),
                submission.getMemoryKb(),
                submission.getLanguage(),
                submission.getCodeBytes(),
                submission.getSubmittedAt()
        );
    }
}
