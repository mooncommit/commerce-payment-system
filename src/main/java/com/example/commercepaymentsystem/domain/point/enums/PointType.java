package com.example.commercepaymentsystem.domain.point.enums;

public enum PointType {
    EARN, //적립
    USE, //사용
    REFUND, //사용복구
    REVOKE; //회수

    private final String reason;

    PointType(String reason)
    {
        this.reason= reason;
    }
}
