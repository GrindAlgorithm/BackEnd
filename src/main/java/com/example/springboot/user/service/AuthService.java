package com.example.springboot.user.service;

import com.example.springboot.user.dto.LoginRequestDTO;
import com.example.springboot.user.dto.SignupRequestDTO;
import com.example.springboot.user.dto.UserDTO;

public interface AuthService {

    /** 즉시 가입 — 중복 검사 + 비밀번호 해싱 후 저장. 실패 시 ApiException. */
    UserDTO signup(SignupRequestDTO request);

    /** 이메일/비밀번호 검증. 실패 시 ApiException(INVALID_CREDENTIALS). */
    UserDTO login(LoginRequestDTO request);

    /** 세션의 인증 principal(email)로 현재 유저 조회 (GET /me). */
    UserDTO getByEmail(String email);
}
