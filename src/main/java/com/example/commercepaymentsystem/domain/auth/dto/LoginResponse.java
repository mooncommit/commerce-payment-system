package com.example.commercepaymentsystem.domain.auth.dto;

import lombok.Getter;

@Getter
public class LoginResponse {

    private final long id;
    private final String email;
    private final String name;
    private final String accessToken;
    private final String refreshToken;

    public LoginResponse(long id, String email, String name, String accessToken, String refreshToken) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
