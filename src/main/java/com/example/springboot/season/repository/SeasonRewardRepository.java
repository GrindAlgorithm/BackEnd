package com.example.springboot.season.repository;

import com.example.springboot.season.entity.SeasonRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonRewardRepository extends JpaRepository<SeasonRewardEntity, String> {

    /** 시즌 리워드 목록 — 화면 노출 순서(sort_order) */
    List<SeasonRewardEntity> findBySeason_IdOrderBySortOrderAsc(Integer seasonId);
}
