package com.example.springboot.user;

import com.example.springboot.user.entity.UserEntity;
import com.example.springboot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 앱 시작 시 관리자 계정을 1개 보장한다(idempotent — 이미 있으면 아무것도 안 함).
 * 자격증명은 설정으로 덮어쓸 수 있다(application-local.yml 의 app.admin.*).
 * 기본값은 개발용이므로 운영 배포 전 반드시 교체할 것.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@grind.dev}")
    private String email;

    @Value("${app.admin.handle:admin}")
    private String handle;

    @Value("${app.admin.password:Admin!234}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(email) || userRepository.existsByHandle(handle)) {
            return;
        }
        UserEntity admin = UserEntity.createAdminEntity(
                handle, email, passwordEncoder.encode(password), LocalDateTime.now());
        userRepository.save(admin);
        log.info("관리자 계정 시드 생성: email={} handle={} (기본 비밀번호 사용 시 운영 전 교체 필요)", email, handle);
    }
}
