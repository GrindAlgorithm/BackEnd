package com.example.springboot.notice.service;

import com.example.springboot.notice.dto.NoticeDetailDTO;
import com.example.springboot.notice.dto.NoticeRequestDTO;

import java.util.List;

/** 관리자 공지 관리 + 공지 상세 조회 (요건 3). 조회는 최신순. */
public interface NoticeService {

    /** 관리 화면용 목록 — 수정 폼에서 body 가 필요해 상세 DTO 로 내린다. */
    List<NoticeDetailDTO> getNotices();

    /** 공지 상세 (본문 포함) — 유저 공지 열람용. */
    NoticeDetailDTO getNotice(long noticeId);

    NoticeDetailDTO createNotice(NoticeRequestDTO request);

    NoticeDetailDTO updateNotice(long noticeId, NoticeRequestDTO request);

    void deleteNotice(long noticeId);
}
