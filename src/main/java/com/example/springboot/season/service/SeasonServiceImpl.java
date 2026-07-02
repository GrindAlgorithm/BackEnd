package com.example.springboot.season.service;

import com.example.springboot.problem.dto.ProblemSummaryDTO;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.season.dto.SeasonDTO;
import com.example.springboot.season.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final ProblemRepository problemRepository;

    @Override
    public List<SeasonDTO> getSeasons() {
        LocalDate today = LocalDate.now();
        return seasonRepository.findAllByOrderByIdDesc().stream()
                .map(season -> SeasonDTO.of(season, today))
                .toList();
    }

    @Override
    public List<ProblemSummaryDTO> getSeasonProblems(Integer seasonId) {
        return problemRepository.findBySeason_IdOrderByDisplayNoAsc(seasonId).stream()
                .map(ProblemSummaryDTO::of)
                .toList();
    }
}
