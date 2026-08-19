package com.example.springboot.discussion.repository;

import com.example.springboot.discussion.entity.DiscussionPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscussionPostRepository extends JpaRepository<DiscussionPostEntity, Long> {

    /** 특정 문제의 토론 글 — 최신순 */
    List<DiscussionPostEntity> findByProblem_ProblemIdOrderByCreatedAtDesc(String problemId);
}
