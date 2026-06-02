package com.example.commercepaymentsystem.domain.refund.entity;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paymentId;

    private Long orderId;

    private Long memberId;

    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;

    private String reason;

    private Long refundTotalAmount;

    private Long refundPointAmount;

    private Long refundPgAmount;
}
