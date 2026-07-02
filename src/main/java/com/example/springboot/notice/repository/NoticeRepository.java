package com.example.springboot.notice.repository;

import com.example.springboot.notice.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    /** 최신 공지 먼저 */
    List<NoticeEntity> findAllByOrderByPublishedAtDesc();
}
