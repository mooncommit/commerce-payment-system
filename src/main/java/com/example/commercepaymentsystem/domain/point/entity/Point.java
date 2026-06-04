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
    private Long balnceAfter;

    private String reason;

}
