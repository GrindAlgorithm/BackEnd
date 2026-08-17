package com.example.springboot.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** POST /auth/login 요청 — 자체 이메일 로그인 (연동 문서 §2.2). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "이메일을 입력해 주세요")
    private String email;

    @NotBlank(message = "비밀번호를 입력해 주세요")
    private String password;
}
