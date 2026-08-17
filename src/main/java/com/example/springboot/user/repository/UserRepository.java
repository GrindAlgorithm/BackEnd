package com.example.springboot.user.repository;

import com.example.springboot.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByHandle(String handle);

    boolean existsByEmail(String email);

    boolean existsByHandle(String handle);
}
