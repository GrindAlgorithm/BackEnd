package com.example.springboot.discussion.dto;

import com.example.springboot.discussion.entity.DiscussionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 토론 글 작성 요청 (요건 4 — POST /problems/{id}/discussions). category 는 소문자 value 로 온다. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionCreateRequestDTO {

    @NotNull(message = "분류를 선택해 주세요")
    private DiscussionCategory category; // code_review | solution

    @NotBlank(message = "제목을 입력해 주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    private String title;

    @NotBlank(message = "본문을 입력해 주세요")
    @Size(max = 20_000, message = "본문은 20,000자 이하여야 합니다")
    private String body; // 마크다운 원문
}
