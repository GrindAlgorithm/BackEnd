package com.example.springboot.user.entity;

/**
 * 유저 권한. 세션 인증 시 "ROLE_" + name() 형태의 GrantedAuthority 로 부여된다
 * (예: ADMIN → ROLE_ADMIN → SecurityConfig 의 hasRole("ADMIN")).
 */
public enum UserRole {
    USER,
    ADMIN
}
