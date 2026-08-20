package com.example.springboot.notice.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.notice.dto.NoticeDetailDTO;
import com.example.springboot.notice.dto.NoticeRequestDTO;
import com.example.springboot.notice.entity.NoticeEntity;
import com.example.springboot.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDetailDTO> getNotices() {
        return noticeRepository.findAllByOrderByPublishedAtDesc().stream()
                .map(NoticeDetailDTO::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDetailDTO getNotice(long noticeId) {
        return noticeRepository.findById(noticeId)
                .map(NoticeDetailDTO::of)
                .orElseThrow(() -> ApiException.notFound("NOTICE_NOT_FOUND", "공지를 찾을 수 없습니다"));
    }

    @Override
    @Transactional
    public NoticeDetailDTO createNotice(NoticeRequestDTO request) {
        NoticeEntity notice = NoticeEntity.createNoticeEntity(
                request.getTag(), request.getTitle(), bodyOrEmpty(request),
                LocalDateTime.now(), request.isHighlight());
        noticeRepository.save(notice);

        if (log.isInfoEnabled()) {
            log.info("createNotice success id={} title={}", notice.getId(), notice.getTitle());
        }
        return NoticeDetailDTO.of(notice);
    }

    @Override
    @Transactional
    public NoticeDetailDTO updateNotice(long noticeId, NoticeRequestDTO request) {
        NoticeEntity notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> ApiException.notFound("NOTICE_NOT_FOUND", "공지를 찾을 수 없습니다"));
        notice.update(request.getTag(), request.getTitle(), bodyOrEmpty(request), request.isHighlight());
        return NoticeDetailDTO.of(notice);
    }

    @Override
    @Transactional
    public void deleteNotice(long noticeId) {
        if (!noticeRepository.existsById(noticeId)) {
            throw ApiException.notFound("NOTICE_NOT_FOUND", "공지를 찾을 수 없습니다");
        }
        noticeRepository.deleteById(noticeId);
    }

    /** 제목만 있는 공지 허용 — body 미입력(null)은 빈 본문으로 저장 (컬럼 NOT NULL). */
    private String bodyOrEmpty(NoticeRequestDTO request) {
        return request.getBody() == null ? "" : request.getBody();
    }
}
