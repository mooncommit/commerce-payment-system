package com.example.commercepaymentsystem.domain.auth.jwt;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import com.example.commercepaymentsystem.global.exception.JwtTokenException;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            jwtProvider.validateToken(token);

            Long memberId = jwtProvider.getMemberId(token);
            String email = jwtProvider.getEmail(token);

            LoginMember loginMember = new LoginMember(memberId, email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            loginMember,
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);

        } catch (JwtTokenException e) {
            ErrorCode errorCode = e.getErrorCode();

            response.setStatus(errorCode.getStatus().value());
            response.setContentType("application/json;charset=UTF-8");

            objectMapper.writeValue(
                    response.getWriter(),
                    ApiResponse.error(errorCode)
            );
        }
    }
}