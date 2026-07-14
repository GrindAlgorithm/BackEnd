package com.example.springboot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder encodePassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf((auth) -> auth.disable());

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/api/v1/example",
                                // 문제목록(시즌/시즌 문제) — 미로그인 열람 허용(연동 문서 §2.6)
                                "/api/v1/seasons/**",
                                // 홈 대시보드 — 인증 연동 전까지 접근 허용(추후 authenticated 로 전환)
                                "/api/v1/dashboard",
                                // 채점 현황/제출 — 문제 탭 + IDE 제출·폴링(연동 문서 §2.10~2.12)
                                "/api/v1/submissions/**",
                                // 코드 실행 (IDE 예제 테스트, 연동 문서 §2.9)
                                "/api/v1/runs/**",
                                // 문제 상세(본문 미포함) — 연동 문서 §2.7.
                                // ⚠ 추후 POST /problems/{id}/open(본문) 추가 시 GET만 허용하도록 좁힐 것
                                "/api/v1/problems/**",
                                // 랭킹 탭 — 연동 문서 §2.13
                                "/api/v1/rankings/**"
                        ).permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 5173 = Vite dev 서버(프론트). 3000 은 기존 설정 유지.
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
