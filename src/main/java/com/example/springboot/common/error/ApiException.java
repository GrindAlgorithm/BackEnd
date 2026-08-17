package com.example.springboot.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 계약 §1.4 에러 봉투({error:{code,message}})로 변환되는 도메인 예외.
 * message 는 유저에게 그대로 노출 가능한 한국어여야 한다.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException badRequest(String code, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static ApiException conflict(String code, String message) {
        return new ApiException(HttpStatus.CONFLICT, code, message);
    }
}
