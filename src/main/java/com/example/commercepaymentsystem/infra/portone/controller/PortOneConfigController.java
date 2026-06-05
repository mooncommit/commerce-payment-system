package com.example.commercepaymentsystem.infra.portone.controller;

import com.example.commercepaymentsystem.global.response.ApiResponse;
import com.example.commercepaymentsystem.infra.portone.config.PortOneProperties;
import com.example.commercepaymentsystem.infra.portone.dto.PortOneConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PortOneConfigController {

    private final PortOneProperties portOneProperties;

    @GetMapping("/api/config/portone")
    public ResponseEntity<ApiResponse<PortOneConfigResponse>> getConfig() {
        return ResponseEntity.ok(ApiResponse.success(new PortOneConfigResponse(
                portOneProperties.getStoreId(),
                portOneProperties.getChannelKey()
        ), "PortOne 설정 조회 성공"));
    }
}
