package com.example.commercepaymentsystem.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/*
ErrorCode Enum 클래스
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseCode {

    // ── Common ──────────────────────────────────────────────
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),

    // ── Member ──────────────────────────────────────────────
    CONFLICT_EMAIL(HttpStatus.CONFLICT, "MEMBER_001", "이미 존재하는 이메일 입니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "ORDER_002", "유효하지 않은 주문 상태 변경입니다."),

    // ── Payment ─────────────────────────────────────────────
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND,          "PAYMENT_001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST,  "PAYMENT_002", "결제 금액이 일치하지 않습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST,   "PAYMENT_003", "유효하지 않은 결제 상태입니다."),
    PAYMENT_NOT_PAID(HttpStatus.BAD_REQUEST,         "PAYMENT_004", "PG사 결제가 완료되지 않았습니다."),
    ALREADY_PROCESSED_PAYMENT(HttpStatus.CONFLICT,   "PAYMENT_005", "이미 처리된 결제입니다."),
    PAYMENT_ID_MISMATCH(HttpStatus.BAD_REQUEST,      "PAYMENT_007", "주문과 결제 식별자가 일치하지 않습니다."),
    PG_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_008", "PG 취소 요청에 실패했습니다."),

    // ── Refund ──────────────────────────────────────────────
    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND,           "REFUND_001", "환불 정보를 찾을 수 없습니다."),
    INVALID_REFUND_STATUS(HttpStatus.BAD_REQUEST,    "REFUND_002", "환불 가능한 결제 상태가 아닙니다."),
    REFUND_QUANTITY_EXCEEDED(HttpStatus.CONFLICT,    "REFUND_003", "잔여 환불 가능 수량을 초과했습니다."),
    INVALID_REFUND_QUANTITY(HttpStatus.BAD_REQUEST,  "REFUND_004", "환불 수량은 1 이상이어야 합니다."),

    // ── Webhook ─────────────────────────────────────────────
    INVALID_WEBHOOK_SIGNATURE(HttpStatus.UNAUTHORIZED, "WEBHOOK_001", "웹훅 서명이 유효하지 않습니다."),
    WEBHOOK_PAYMENT_ID_MISSING(HttpStatus.BAD_REQUEST, "WEBHOOK_002", "portonePaymentId를 가져올 수 없습니다.");


    private final HttpStatus status;  // HTTP 상태 코드
    private final String code;        // 커스텀 에러 코드 (예: "COMMON_001")
    private final String message;     // 에러 메시지
}