package com.example.commercepaymentsystem.domain.auth.dto;

import com.example.commercepaymentsystem.domain.member.enums.MemberShip;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateMemberResponse {

    private final Long id;
    private final String email;
    private final String name;
    private final String phoneNumber;
    private final Long pointBalance;
    private final MemberShip memberShip;
    private final LocalDateTime createdAt;

    public CreateMemberResponse(Long id, String email, String name, String phoneNumber, Long pointBalance, MemberShip memberShip, LocalDateTime createAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.pointBalance = pointBalance;
        this.memberShip = memberShip;
        this.createdAt = createAt;
    }
}
