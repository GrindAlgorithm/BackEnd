package com.example.springboot.user;

import com.example.springboot.user.entity.UserEntity;
import com.example.springboot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 현재 로그인 유저 조회 헬퍼. 세션 인증 principal(email)을 handle 로 변환한다.
 * 비로그인/익명 요청이면 null 을 반환한다(랭킹·제출 등에서 "내 것" 판단에 사용).
 */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    /** 로그인 유저 handle. 비로그인이면 null. */
    public String currentHandle() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByEmail(auth.getName())
                .map(UserEntity::getHandle)
                .orElse(null);
    }
}
