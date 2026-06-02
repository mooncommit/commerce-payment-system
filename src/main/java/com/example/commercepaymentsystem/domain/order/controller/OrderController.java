package com.example.commercepaymentsystem.domain.order.controller;

import com.example.commercepaymentsystem.domain.order.dto.response.PreviewOrderResponse;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/preview")
    public ApiResponse<PreviewOrderResponse> preview(@RequestParam(required = false) List<Long> cartItemIds) {
        PreviewOrderResponse response = orderService.preview(cartItemIds);
        return ApiResponse.success(response, "주문서 미리보기 조회 성공");
    }
}
