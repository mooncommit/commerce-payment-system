package com.example.commercepaymentsystem.domain.member.entity;

import com.example.commercepaymentsystem.domain.member.enums.MemberShip;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private Long pointBalance = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberShip memberShip;

    public void increasePoint(Long amount) {
        this.pointBalance += amount;
    }

    public void decreasePoint(Long amount) {
        if (this.pointBalance < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }

        this.pointBalance -= amount;
    }

    public Member(String email, String passwordHash, String name, String phoneNumber) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.memberShip = MemberShip.NORMAL;
    }
}
