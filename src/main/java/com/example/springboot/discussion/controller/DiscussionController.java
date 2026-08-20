package com.example.springboot.discussion.controller;

import com.example.springboot.discussion.dto.DiscussionCreateRequestDTO;
import com.example.springboot.discussion.dto.DiscussionPostDetailDTO;
import com.example.springboot.discussion.dto.DiscussionResponseDTO;
import com.example.springboot.discussion.service.DiscussionService;
import com.example.springboot.util.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 문제 토론 (연동 문서 §2.14 + 요건 4). 목록은 403 대신 200 + accessible 분기:
 * 미해결 유저는 통계만, 해당 문제 Accepted 유저는 글 목록까지.
 * 작성/상세는 정답자 한정 — 미해결이면 403 DISCUSSION_LOCKED.
 */
@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
@Slf4j
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping("/{problemId}/discussions")
    public ResponseResult<DiscussionResponseDTO> getDiscussions(@PathVariable String problemId) {
        DiscussionResponseDTO discussion = discussionService.getDiscussion(problemId);

        if (log.isInfoEnabled()) {
            log.info("getDiscussions Controller Success : problemId={}, accessible={}",
                    problemId, discussion.isAccessible());
        }
        return ResponseResult.success(discussion);
    }

    /** GET — 토론 글 상세(마크다운 body 포함). */
    @GetMapping("/{problemId}/discussions/{postId}")
    public ResponseResult<DiscussionPostDetailDTO> getPost(@PathVariable String problemId,
                                                           @PathVariable long postId) {
        return ResponseResult.success(discussionService.getPost(problemId, postId));
    }

    /** POST — 토론 글 작성 (요건 4). */
    @PostMapping("/{problemId}/discussions")
    public ResponseResult<DiscussionPostDetailDTO> createPost(@PathVariable String problemId,
                                                              @Valid @RequestBody DiscussionCreateRequestDTO request) {
        return ResponseResult.success(discussionService.createPost(problemId, request));
    }
}
