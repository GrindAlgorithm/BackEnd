package com.example.springboot.user.controller;

import com.example.springboot.user.CurrentUserProvider;
import com.example.springboot.user.dto.TitleUpdateRequestDTO;
import com.example.springboot.user.dto.UserProfileDTO;
import com.example.springboot.user.dto.UserProfileResponseDTO;
import com.example.springboot.user.service.UserProfileService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 유저 프로필/칭호 — 연동 문서 §2.15(GET /users/{handle})·§2.16(PUT /me/title) */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final CurrentUserProvider currentUserProvider;

    /** GET /users/{handle} — 유저 프로필. 미로그인 열람 허용(백준식), isMe 는 세션 기준. */
    @GetMapping("/users/{handle}")
    public ResponseResult<UserProfileResponseDTO> getProfile(@PathVariable String handle) {
        String viewerHandle = currentUserProvider.currentHandle();
        UserProfileDTO profile = userProfileService.getProfile(handle, viewerHandle);
        if (profile == null) {
            return ResponseResult.<UserProfileResponseDTO>error(null);
        }

        if (log.isInfoEnabled()) {
            log.info("getProfile Controller Success : handle={}, isMe={}", handle, profile.isMe());
        }
        return ResponseResult.success(UserProfileResponseDTO.of(profile));
    }

    /** PUT /me/title — 대표 칭호 선택/해제. 성공 204, 미보유 칭호 지정은 400 (§2.16). */
    @PutMapping("/me/title")
    public ResponseEntity<Void> updateTitle(@RequestBody TitleUpdateRequestDTO request,
                                            Authentication authentication) {
        boolean updated = userProfileService.updateTitle(authentication.getName(), request.getTitleId());
        return updated ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    }
}
