package com.example.commercepaymentsystem.domain.point.controller;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.point.dto.GetMyPointResponse;
import com.example.commercepaymentsystem.domain.point.dto.PointHistoryResponse;
import com.example.commercepaymentsystem.domain.point.service.PointService;
import com.example.commercepaymentsystem.global.dto.PageResponse;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<GetMyPointResponse>> getBalance(@AuthenticationPrincipal LoginMember loginMember)
    {
        GetMyPointResponse result = pointService.getMyPoint(loginMember.getMemberId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(result,"포인트 잔액 조회 성공"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<PointHistoryResponse>>> getHistory(@AuthenticationPrincipal LoginMember loginMember,
                                                                                      @RequestParam(defaultValue = "1") int page,
                                                                                      @RequestParam(defaultValue = "10") int size)
    {
        PageResponse<PointHistoryResponse> result = pointService.getMyHistory(loginMember.getMemberId(),page,size);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(result,"포인트 거래내역 조회 성공"));
    }
}
