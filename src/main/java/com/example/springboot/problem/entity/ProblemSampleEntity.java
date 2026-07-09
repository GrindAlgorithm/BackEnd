package com.example.springboot.problem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 문제 예제 입출력 (본문에 포함, 연동 문서 §2.8 body.samples). problem 과 1:N. */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "problem_sample")
public class ProblemSampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemEntity problem;

    /** 예제 순번(1부터) */
    @Column(nullable = false)
    private int ordinal;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String output;

    public static ProblemSampleEntity createProblemSampleEntity(ProblemEntity problem, int ordinal,
                                                                String input, String output) {
        return new ProblemSampleEntity(null, problem, ordinal, input, output);
    }
}
