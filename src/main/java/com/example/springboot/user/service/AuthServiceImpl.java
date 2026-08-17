package com.example.springboot.user.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.user.dto.LoginRequestDTO;
import com.example.springboot.user.dto.SignupRequestDTO;
import com.example.springboot.user.dto.UserDTO;
import com.example.springboot.user.entity.UserEntity;
import com.example.springboot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO signup(SignupRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("EMAIL_TAKEN", "이미 사용 중인 이메일입니다");
        }
        if (userRepository.existsByHandle(request.getHandle())) {
            throw ApiException.conflict("HANDLE_TAKEN", "이미 사용 중인 닉네임입니다");
        }

        UserEntity user = UserEntity.createUserEntity(
                request.getHandle(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                LocalDateTime.now());
        userRepository.save(user);

        if (log.isInfoEnabled()) {
            log.info("signup success handle={} email={}", user.getHandle(), user.getEmail());
        }
        return UserDTO.of(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.badRequest("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다");
        }
        return UserDTO.of(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "세션이 유효하지 않습니다"));
        return UserDTO.of(user);
    }
}
