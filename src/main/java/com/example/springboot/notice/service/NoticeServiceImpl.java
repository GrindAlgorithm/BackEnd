package com.example.springboot.notice.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.notice.dto.NoticeDTO;
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
    public List<NoticeDTO> getNotices() {
        return noticeRepository.findAllByOrderByPublishedAtDesc().stream()
                .map(NoticeDTO::of)
                .toList();
    }

    @Override
    @Transactional
    public NoticeDTO createNotice(NoticeRequestDTO request) {
        NoticeEntity notice = NoticeEntity.createNoticeEntity(
                request.getTag(), request.getTitle(), LocalDateTime.now(), request.isHighlight());
        noticeRepository.save(notice);

        if (log.isInfoEnabled()) {
            log.info("createNotice success id={} title={}", notice.getId(), notice.getTitle());
        }
        return NoticeDTO.of(notice);
    }

    @Override
    @Transactional
    public NoticeDTO updateNotice(long noticeId, NoticeRequestDTO request) {
        NoticeEntity notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> ApiException.notFound("NOTICE_NOT_FOUND", "공지를 찾을 수 없습니다"));
        notice.update(request.getTag(), request.getTitle(), request.isHighlight());
        return NoticeDTO.of(notice);
    }

    @Override
    @Transactional
    public void deleteNotice(long noticeId) {
        if (!noticeRepository.existsById(noticeId)) {
            throw ApiException.notFound("NOTICE_NOT_FOUND", "공지를 찾을 수 없습니다");
        }
        noticeRepository.deleteById(noticeId);
    }
}
