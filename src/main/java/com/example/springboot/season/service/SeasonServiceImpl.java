package com.example.springboot.season.service;

import com.example.springboot.problem.dto.ProblemSummaryResponseDTO;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.season.dto.SeasonResponseDTO;
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
@Transactional(readOnly = true)
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final ProblemRepository problemRepository;

    @Override
    public List<SeasonResponseDTO> getSeasons() {
        LocalDate today = LocalDate.now();
        return seasonRepository.findAllByOrderByIdDesc().stream()
                .map(season -> SeasonResponseDTO.of(season, today))
                .toList();
    }

    @Override
    public List<ProblemSummaryResponseDTO> getSeasonProblems(Integer seasonId) {
        return problemRepository.findBySeason_IdOrderByDisplayNoAsc(seasonId).stream()
                .map(ProblemSummaryResponseDTO::of)
                .toList();
    }
}
