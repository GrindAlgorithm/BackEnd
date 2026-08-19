package com.example.springboot.notice.service;

import com.example.springboot.notice.dto.NoticeDTO;
import com.example.springboot.notice.dto.NoticeRequestDTO;

import java.util.List;

/** 관리자 공지 관리 (요건 3). 조회는 최신순. */
public interface NoticeService {

    List<NoticeDTO> getNotices();

    NoticeDTO createNotice(NoticeRequestDTO request);

    NoticeDTO updateNotice(long noticeId, NoticeRequestDTO request);

    void deleteNotice(long noticeId);
}
