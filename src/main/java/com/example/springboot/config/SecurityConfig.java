package com.example.springboot.config;

import com.example.springboot.user.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    /** OAuth2 로그인 성공 후 리다이렉트할 프론트 경로. dev는 프록시라 "/"로 충분. */
    @Value("${app.oauth2.success-redirect:/}")
    private String oauthSuccessRedirect;

    @Bean
    public BCryptPasswordEncoder encodePassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf((auth) -> auth.disable())
                // REST + 세션 쿠키 인증: 폼 로그인/기본 인증 비활성, 미인증은 401(계약 §1.4)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(this::writeUnauthorized));

        http
                .authorizeHttpRequests((auth) -> auth
                        // 풀이 세션 발급·실행·제출은 로그인 필수 — 아래 permitAll 보다 먼저 매칭돼야 한다.
                        // 세션 만료 후 제출이 익명(anonymous)으로 조용히 기록돼 점수가 유실되던 문제 차단.
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/problems/*/open",
                                "/api/v1/runs",
                                "/api/v1/submissions"
                        ).authenticated()
                        .requestMatchers(
                                // 컨트롤러 예외·404·405 가 포워드되는 경로. 막으면 모든 에러가 빈 401로 위장된다
                                "/error",
                                "/api/v1/example",
                                // 인증 — 로그인/회원가입/로그아웃(미인증 접근 허용). /me 는 인증 필요
                                "/api/v1/auth/**",
                                // 소셜 로그인 진입/콜백 — Spring Security OAuth2 표준 경로(연동 문서 §1.3)
                                "/oauth2/**",
                                "/login/oauth2/**",
                                // 문제목록(시즌/시즌 문제) — 미로그인 열람 허용(연동 문서 §2.6)
                                "/api/v1/seasons/**",
                                // 홈 대시보드 — 인증 연동 전까지 접근 허용(추후 authenticated 로 전환)
                                "/api/v1/dashboard",
                                // 채점 현황·폴링 — 문제 탭(연동 문서 §2.11~2.12). 제출(POST)은 위에서 인증 필수
                                "/api/v1/submissions/**",
                                // 문제 상세(본문 미포함) — 연동 문서 §2.7. 본문 열람(POST open)은 위에서 인증 필수
                                "/api/v1/problems/**",
                                // 랭킹 탭 — 연동 문서 §2.13
                                "/api/v1/rankings/**",
                                // 지원 언어 목록(요건 24) · 공지 상세 — 미로그인 열람 허용
                                "/api/v1/languages/**",
                                "/api/v1/notices/**",
                                // 유저 프로필 — 미로그인 열람 허용(§2.15). PUT /me/title 은 인증 필요
                                "/api/v1/users/**"
                        ).permitAll()
                        // 관리자 전용 — 공지 작성 등(요건 3). ADMIN 권한 필요
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated());

        // 소셜 로그인 — client-id/secret(ClientRegistrationRepository)이 설정된 경우에만 활성화.
        // 미설정 시 앱은 정상 기동하고 소셜만 비활성(자격증명은 application-local.yml 에서 주입).
        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                    .defaultSuccessUrl(oauthSuccessRedirect, true));
        }

        return http.build();
    }

    /** 미인증 → 401 + 계약 §1.4 에러 봉투. 본문이 있어야 프론트가 원인을 그대로 보여 줄 수 있다. */
    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                   AuthenticationException ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"로그인이 필요합니다. 다시 로그인해 주세요\"}}");
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
