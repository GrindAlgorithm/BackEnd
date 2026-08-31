package com.example.springboot.config;

import com.example.springboot.user.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
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
                                // 채점 현황/제출 — 문제 탭 + IDE 제출·폴링(연동 문서 §2.10~2.12)
                                "/api/v1/submissions/**",
                                // 코드 실행 (IDE 예제 테스트, 연동 문서 §2.9)
                                "/api/v1/runs/**",
                                // 문제 상세(본문 미포함) — 연동 문서 §2.7.
                                // ⚠ 추후 POST /problems/{id}/open(본문) 추가 시 GET만 허용하도록 좁힐 것
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
