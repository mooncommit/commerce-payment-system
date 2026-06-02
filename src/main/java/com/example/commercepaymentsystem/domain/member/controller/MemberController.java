package com.example.commercepaymentsystem.domain.member.controller;

import com.example.commercepaymentsystem.domain.member.dto.CreateMemberRequest;
import com.example.commercepaymentsystem.domain.member.dto.CreateMemberResponse;
import com.example.commercepaymentsystem.domain.member.service.MemberService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<CreateMemberResponse>> signup(
            @Valid @RequestBody CreateMemberRequest request
    ) {
        CreateMemberResponse result = memberService.signupMember(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "회원가입성공"
        ));
    }


}
