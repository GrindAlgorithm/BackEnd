package com.example.springboot.user;

import com.example.springboot.user.entity.AuthProvider;
import com.example.springboot.user.entity.UserEntity;
import com.example.springboot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 소셜(OAuth2) 로그인 — GitHub/Google (연동 문서 §1.3).
 * 성공 시 email 로 기존 유저를 찾고, 없으면 자동 가입한다("가입 마찰↓").
 * principal name 을 email 로 맞춰 기존 세션 인증(/me, SecurityConfig)과 호환시킨다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(userRequest);
        AuthProvider provider = AuthProvider.fromRegistrationId(
                userRequest.getClientRegistration().getRegistrationId());

        String email = extractEmail(provider, oauthUser);
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("소셜 계정에서 이메일을 가져올 수 없습니다");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> register(email, provider, oauthUser));

        // principal name 을 email 로 통일 → AuthController.me(authentication.getName()==email) 와 호환
        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());
        attributes.putIfAbsent("email", email);
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes,
                "email");
    }

    private UserEntity register(String email, AuthProvider provider, OAuth2User oauthUser) {
        // ⚠ getAttribute 는 제네릭 <A> A — String.valueOf(...) 에 직접 넘기면 valueOf(char[])
        //   오버로드로 추론돼 ClassCastException(String→char[]) 이 난다. Object 변수로 받는다.
        String base;
        if (provider == AuthProvider.GITHUB) {
            Object login = oauthUser.getAttribute("login");
            base = login == null ? "" : login.toString();
        } else {
            base = email.substring(0, email.indexOf('@'));
        }
        String handle = uniqueHandle(sanitize(base));

        UserEntity user = UserEntity.createOAuthUser(handle, email, provider, LocalDateTime.now());
        userRepository.save(user);
        if (log.isInfoEnabled()) {
            log.info("OAuth 자동가입 provider={} handle={} email={}", provider, handle, email);
        }
        return user;
    }

    /** provider 별 이메일 추출. GitHub 은 이메일이 private 이면 noreply 주소로 대체. */
    private String extractEmail(AuthProvider provider, OAuth2User user) {
        Object email = user.getAttribute("email");
        if (email != null) {
            return email.toString();
        }
        if (provider == AuthProvider.GITHUB) {
            Object login = user.getAttribute("login");
            Object id = user.getAttribute("id");
            if (login != null) {
                return id + "+" + login + "@users.noreply.github.com";
            }
        }
        return null;
    }

    /** handle 후보 정규화 — 영문/숫자/밑줄만, 2~20자. */
    private String sanitize(String base) {
        String s = base == null ? "" : base.replaceAll("[^A-Za-z0-9_]", "");
        if (s.length() < 2) {
            s = "user";
        }
        return s.length() > 20 ? s.substring(0, 20) : s;
    }

    /** 닉네임 중복 시 숫자 suffix. */
    private String uniqueHandle(String base) {
        String handle = base;
        int suffix = 1;
        while (userRepository.existsByHandle(handle)) {
            handle = base + suffix++;
        }
        return handle;
    }
}
