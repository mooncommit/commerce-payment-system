package com.example.commercepaymentsystem.domain.member.dto;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.enums.MemberShip;
import lombok.Getter;

@Getter
public class MemberProfileResponse {

    private final Long memberId;
    private final String email;
    private final String name;
    private final String phoneNumber;
    private final Long pointBalance;
    private final MemberShip memberShip;

    public MemberProfileResponse(Member member) {
        this.memberId = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.phoneNumber = member.getPhoneNumber();
        this.pointBalance = member.getPointBalance();
        this.memberShip = member.getMemberShip();
    }
}
