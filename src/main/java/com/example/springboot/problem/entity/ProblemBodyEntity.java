package com.example.springboot.problem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제 본문 (설명/입력/출력). IDE 진입 시에만 노출(B2) — 연동 문서 §2.8.
 * problem 과 1:1.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "problem_body")
public class ProblemBodyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false, unique = true)
    private ProblemEntity problem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "input_spec", nullable = false, columnDefinition = "TEXT")
    private String inputSpec;

    @Column(name = "output_spec", nullable = false, columnDefinition = "TEXT")
    private String outputSpec;

    public static ProblemBodyEntity createProblemBodyEntity(ProblemEntity problem, String description,
                                                            String inputSpec, String outputSpec) {
        return new ProblemBodyEntity(null, problem, description, inputSpec, outputSpec);
    }
}
