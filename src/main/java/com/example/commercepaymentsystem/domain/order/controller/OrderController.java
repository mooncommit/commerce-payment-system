package com.example.commercepaymentsystem.domain.order.controller;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.order.dto.OrderCancelResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderDetailResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderListResponse;
import com.example.commercepaymentsystem.domain.order.dto.PreviewOrderResponse;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import com.example.commercepaymentsystem.global.dto.PageResponse;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 내 주문 내역 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderListResponse>>> getOrders(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<OrderListResponse> response = orderService.getOrders(loginMember.getMemberId(), page, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "주문 내역 조회 성공"));
    }

    // 주문서 미리보기
    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<PreviewOrderResponse>> previewOrder(
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        PreviewOrderResponse response = orderService.previewOrder(loginMember.getMemberId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "주문서 미리보기 조회 성공"));
    }

    // 내 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long orderId
    ) {
        OrderDetailResponse response = orderService.getOrderDetail(loginMember.getMemberId(), orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "주문 상세 조회 성공"));
    }

    // 결제대기 주문 취소
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderCancelResponse>> cancelPendingOrder(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long orderId
    ) {
        OrderCancelResponse response = orderService.cancelPendingOrder(loginMember.getMemberId(), orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "주문 취소 성공"));
    }

    // 상품 상세에서 바로 주문할 때 주문과 결제 대기 데이터 함께 생성
    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createDirectOrder(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createDirectOrder(loginMember.getMemberId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "상품 바로 주문 및 결제 대기 생성 성공"));
    }
}
