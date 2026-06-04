package com.example.commercepaymentsystem.domain.auth.jwt;

import com.example.commercepaymentsystem.global.exception.ErrorCode;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
/**
 인증되지 않은 사용자가 보호된 API에 접근했을 때 실행되는 메서드
 - Authorization 헤더가 없음
 - 로그인하지 않고 /api/cart 같은 API 접근
  공통 응답 형식으로 401 응답을 내려준다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.error(ErrorCode.TOKEN_NOT_FOUND)
        );
    }
}