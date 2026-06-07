package com.example.commercepaymentsystem.domain.point.dto;


import lombok.Getter;

@Getter
public class GetMyPointResponse {

    private final long memberId;
    private final long pointBalance;

    public GetMyPointResponse(long memberId, long pointBalance) {
        this.memberId = memberId;
        this.pointBalance = pointBalance;
    }
}
