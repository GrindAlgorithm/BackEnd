package com.example.springboot.judge0;

/**
 * Judge0 실행 판정(정규화). Judge0 status.id 를 백엔드 도메인으로 변환한 값.
 * (3=Accepted, 4=Wrong Answer, 5=TLE, 6=Compile Error, 7~12=Runtime Error, 그 외=Internal)
 */
public enum Judge0Verdict {
    ACCEPTED,
    WRONG_ANSWER,
    TIME_LIMIT,
    MEMORY_LIMIT,
    RUNTIME_ERROR,
    COMPILE_ERROR,
    INTERNAL_ERROR;

    /** Judge0 status.id → Verdict */
    public static Judge0Verdict fromStatusId(int statusId) {
        return switch (statusId) {
            case 3 -> ACCEPTED;
            case 4 -> WRONG_ANSWER;
            case 5 -> TIME_LIMIT;
            case 6 -> COMPILE_ERROR;
            case 7, 8, 9, 10, 11, 12 -> RUNTIME_ERROR;
            default -> INTERNAL_ERROR; // 1,2(In Queue/Processing)는 wait=true 에선 오지 않음
        };
    }
}
