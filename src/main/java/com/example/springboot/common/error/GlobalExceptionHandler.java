package com.example.springboot.common.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 계약 §1.4 에러 봉투로 변환하는 전역 예외 핸들러.
 * ApiException(도메인 예외)과 @Valid 검증 실패만 처리한다 — 나머지는 기존 동작 유지.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ErrorResponse.of(ex.getCode(), ex.getMessage()));
    }

    /** @Valid 실패 → 400 VALIDATION_FAILED (첫 필드 에러 메시지를 유저에게 노출). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError first = ex.getBindingResult().getFieldError();
        String message = first != null ? first.getDefaultMessage() : "입력값이 올바르지 않습니다";
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_FAILED", message));
    }
}
