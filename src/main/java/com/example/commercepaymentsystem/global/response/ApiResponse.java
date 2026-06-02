package com.example.commercepaymentsystem.global.response;

import com.example.commercepaymentsystem.global.exception.BaseCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
// 값이 null인 필드는 JSON 응답에서 제외합니다. (성공 시 error 제외, 에러 시 data, message 제외)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final ErrorData error;

    private ApiResponse(boolean success, T data, String message, ErrorData error) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
    }

    // ── 성공 응답 ────────────────────────────────────────────

    // data와 메시지가 모두 있는 성공 응답
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    // data가 없고 메시지만 있는 성공 응답 (수정, 삭제 등)
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, null, message, null);
    }

    // ── 실패 응답 ────────────────────────────────────────────

    // BaseCode 기반 실패
    public static ApiResponse<Void> error(BaseCode baseCode) {
        return new ApiResponse<>(false, null, null, new ErrorData(baseCode.getCode(), baseCode.getMessage()));
    }

    // BaseCode + 커스텀 메시지 (검증 실패 등 상세 메시지 덮어쓰기)
    public static ApiResponse<Void> error(BaseCode baseCode, String customMessage) {
        return new ApiResponse<>(false, null, null, new ErrorData(baseCode.getCode(), customMessage));
    }

    // ── 내부 클래스 (에러 정보 캡슐화) ────────────────────────────
    @Getter
    @AllArgsConstructor
    public static class ErrorData {
        private final String code;
        private final String message;
    }
}