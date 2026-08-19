package com.example.springboot.recommendation.repository;

import com.example.springboot.recommendation.entity.RecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {

    /** 추천 순위(rank_no) 오름차순 — 곧 노출 순서 */
    List<RecommendationEntity> findAllByOrderByRankNoAsc();
}
