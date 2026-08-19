package com.example.springboot.user.entity;

/**
 * 인증 제공자. LOCAL=자체 이메일/비밀번호, 나머지는 OAuth2 소셜 로그인.
 * OAuth2 registrationId(github/google)와 대응한다.
 */
public enum AuthProvider {
    LOCAL,
    GITHUB,
    GOOGLE;

    /** OAuth2 registrationId("github"/"google") → provider */
    public static AuthProvider fromRegistrationId(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "github" -> GITHUB;
            case "google" -> GOOGLE;
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인: " + registrationId);
        };
    }
}
