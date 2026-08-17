package com.example.springboot.user.dto;

import com.example.springboot.user.entity.UserEntity;
import com.example.springboot.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 서비스 계층 내부 유저 모델 (Entity → UserDTO → MeResponseDTO). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String handle;
    private String email;
    private UserRole role;
    private String selectedTitleId;
    private LocalDateTime joinedAt;

    public static UserDTO of(UserEntity entity) {
        return new UserDTO(
                entity.getId(),
                entity.getHandle(),
                entity.getEmail(),
                entity.getRole(),
                entity.getSelectedTitleId(),
                entity.getJoinedAt());
    }
}
