package com.example.springboot.discussion.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.discussion.dto.DiscussionPostDTO;
import com.example.springboot.discussion.dto.DiscussionResponseDTO;
import com.example.springboot.discussion.dto.DiscussionStatsDTO;
import com.example.springboot.discussion.entity.DiscussionCategory;
import com.example.springboot.discussion.entity.DiscussionPostEntity;
import com.example.springboot.discussion.repository.DiscussionPostRepository;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DiscussionServiceImpl implements DiscussionService {

    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    private final DiscussionPostRepository discussionPostRepository;
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public DiscussionResponseDTO getDiscussion(String problemId) {
        if (problemRepository.findByProblemId(problemId).isEmpty()) {
            throw ApiException.notFound("PROBLEM_NOT_FOUND", "문제를 찾을 수 없습니다");
        }

        List<DiscussionPostEntity> allPosts =
                discussionPostRepository.findByProblem_ProblemIdOrderByCreatedAtDesc(problemId);
        DiscussionStatsDTO stats = buildStats(allPosts);

        // 접근 판정: 로그인 유저가 이 문제를 Accepted 한 이력이 있는가
        String myHandle = currentUserProvider.currentHandle();
        boolean accessible = myHandle != null
                && submissionRepository.existsByProblem_ProblemIdAndUserHandleAndStatus(
                        problemId, myHandle, SubmissionStatus.ACCEPTED);

        if (!accessible) {
            return DiscussionResponseDTO.locked(stats);
        }

        String firstSolvedAt = submissionRepository
                .findFirstByProblem_ProblemIdAndUserHandleAndStatusOrderBySubmittedAtAsc(
                        problemId, myHandle, SubmissionStatus.ACCEPTED)
                .map(s -> s.getSubmittedAt().atOffset(KST).toString())
                .orElse(null);
        List<DiscussionPostDTO> posts = allPosts.stream().map(DiscussionPostDTO::of).toList();

        return DiscussionResponseDTO.unlocked(firstSolvedAt, stats, posts);
    }

    /** category 별 집계 — 현재 축은 solution / code_review 둘뿐(연동 문서 §2.14). */
    private DiscussionStatsDTO buildStats(List<DiscussionPostEntity> posts) {
        long solution = posts.stream().filter(p -> p.getCategory() == DiscussionCategory.SOLUTION).count();
        long codeReview = posts.stream().filter(p -> p.getCategory() == DiscussionCategory.CODE_REVIEW).count();
        return new DiscussionStatsDTO(posts.size(), solution, codeReview);
    }
}
