package com.example.commercepaymentsystem.domain.point.entity;

import com.example.commercepaymentsystem.domain.point.enums.PointType;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_ledger")
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/*
원장 테이블 성격상
객체가 필요한게아닌 id값만 필요함
 */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType pointType;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long balanceAfter;

    private String reason;

    public Point(Long memberId,Long paymentId,PointType pointType,Long amount,Long balanceAfter,String reason)
    {
        this.memberId=memberId;
        this.paymentId=paymentId;
        this.pointType = pointType;
        this.amount=amount;
        this.balanceAfter = balanceAfter;
        this.reason=reason;
    }
}
