package com.example.springboot.discussion.controller;

import com.example.springboot.discussion.dto.DiscussionResponseDTO;
import com.example.springboot.discussion.service.DiscussionService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 문제 토론 (연동 문서 §2.14). 403 대신 200 + accessible 분기:
 * 미해결 유저는 통계만, 해당 문제 Accepted 유저는 글 목록까지.
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
}
