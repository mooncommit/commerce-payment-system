package com.example.commercepaymentsystem.domain.auth.dto;

import lombok.Getter;

@Getter
public class LoginMember {

    private final long memberId;
    private final String email;

    public LoginMember(long memberId, String email) {
        this.memberId = memberId;
        this.email = email;
    }
}
