package com.example.commercepaymentsystem.domain.auth.controller;

import com.example.commercepaymentsystem.domain.auth.dto.*;
import com.example.commercepaymentsystem.domain.auth.service.AuthService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<CreateMemberResponse>> signup(
            @Valid @RequestBody CreateMemberRequest request
    ) {
        CreateMemberResponse result = authService.signupMember(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "회원가입성공"
        ));
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request)
    {
        LoginResponse result = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(result,"로그인 되었습니다"));
    }

    //토큰 재발급 api
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @RequestBody TokenRequest request
    ) {
        TokenResponse result = authService.reissue(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(result,"토큰 재발급 완료"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal LoginMember loginMember)
    {
        authService.logout(loginMember.getMemberId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("로그아웃 되었습니다"));
    }
}
