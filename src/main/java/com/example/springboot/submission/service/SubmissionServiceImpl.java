package com.example.springboot.submission.service;

import com.example.springboot.submission.dto.SubmissionDTO;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;

    @Override
    public List<SubmissionDTO> getSubmissions(Integer seasonId, String problemId, boolean mine) {
        // TODO: mine 은 유저/인증 도메인 연동 시 "현재 유저 handle" 필터로 적용. 현재는 무시.
        List<SubmissionEntity> submissions = findSubmissions(seasonId, problemId);
        return submissions.stream()
                .map(SubmissionDTO::of)
                .toList();
    }

    private List<SubmissionEntity> findSubmissions(Integer seasonId, String problemId) {
        if (seasonId != null && problemId != null) {
            return submissionRepository.findTop50ByProblem_Season_IdAndProblem_ProblemIdOrderBySubmittedAtDesc(seasonId, problemId);
        }
        if (seasonId != null) {
            return submissionRepository.findTop50ByProblem_Season_IdOrderBySubmittedAtDesc(seasonId);
        }
        if (problemId != null) {
            return submissionRepository.findTop50ByProblem_ProblemIdOrderBySubmittedAtDesc(problemId);
        }
        return submissionRepository.findTop50ByOrderBySubmittedAtDesc();
    }
}
