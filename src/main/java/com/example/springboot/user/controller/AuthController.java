package com.example.springboot.user.controller;

import com.example.springboot.user.dto.LoginRequestDTO;
import com.example.springboot.user.dto.MeResponseDTO;
import com.example.springboot.user.dto.SignupRequestDTO;
import com.example.springboot.user.dto.UserDTO;
import com.example.springboot.user.service.AuthService;
import com.example.springboot.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 인증 — 자체 이메일 로그인/회원가입/로그아웃/내 정보 (연동 문서 §2.1~2.3 + signup).
 * 세션 쿠키(JSESSIONID) 기반: 로그인/가입 성공 시 SecurityContext 를 세션에 저장한다.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    /** POST /auth/signup — 즉시 가입 후 바로 로그인 상태로 만든다. */
    @PostMapping("/auth/signup")
    public ResponseResult<MeResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request,
                                                HttpServletRequest req, HttpServletResponse res) {
        UserDTO user = authService.signup(request);
        establishSession(user, req, res);
        return ResponseResult.success(MeResponseDTO.of(user));
    }

    /** POST /auth/login — 이메일/비밀번호 로그인, 세션 발급. */
    @PostMapping("/auth/login")
    public ResponseResult<MeResponseDTO> login(@Valid @RequestBody LoginRequestDTO request,
                                               HttpServletRequest req, HttpServletResponse res) {
        UserDTO user = authService.login(request);
        establishSession(user, req, res);

        if (log.isInfoEnabled()) {
            log.info("login success handle={}", user.getHandle());
        }
        return ResponseResult.success(MeResponseDTO.of(user));
    }

    /** POST /auth/logout — 세션 무효화. 본문 없음(204). */
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(req, res, auth);
        return ResponseEntity.noContent().build();
    }

    /** GET /me — 내 정보(세션 확인). 미인증이면 SecurityConfig 가 401 로 차단. */
    @GetMapping("/me")
    public ResponseResult<MeResponseDTO> me(Authentication authentication) {
        UserDTO user = authService.getByEmail(authentication.getName());
        return ResponseResult.success(MeResponseDTO.of(user));
    }

    /**
     * 인증 성공 principal(email) + 권한(ROLE_xxx)을 SecurityContext 에 담아
     * 세션(JSESSIONID)에 저장한다. 권한은 SecurityConfig 의 hasRole 인가에 쓰인다.
     */
    private void establishSession(UserDTO user, HttpServletRequest req, HttpServletResponse res) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, req, res);
    }
}
