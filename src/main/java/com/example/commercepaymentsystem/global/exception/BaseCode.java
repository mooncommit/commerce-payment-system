package com.example.commercepaymentsystem.global.exception;

import org.springframework.http.HttpStatus;

public interface BaseCode {
    HttpStatus getStatus();
    String getMessage();
    String getCode();
}

