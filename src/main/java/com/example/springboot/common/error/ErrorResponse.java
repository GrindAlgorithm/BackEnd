package com.example.springboot.common.error;

/**
 * 계약 §1.4 에러 봉투. 4xx/5xx 응답 본문 형태:
 * { "error": { "code": "INVALID_CREDENTIALS", "message": "..." } }
 */
public record ErrorResponse(ErrorBody error) {

    public record ErrorBody(String code, String message) {
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new ErrorBody(code, message));
    }
}
