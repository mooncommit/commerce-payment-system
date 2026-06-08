package com.example.commercepaymentsystem.domain.point.service;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.point.dto.GetMyPointResponse;
import com.example.commercepaymentsystem.domain.point.dto.PointHistoryResponse;
import com.example.commercepaymentsystem.domain.point.entity.Point;
import com.example.commercepaymentsystem.domain.point.enums.PointType;
import com.example.commercepaymentsystem.domain.point.repository.PointRepository;
import com.example.commercepaymentsystem.global.dto.PageResponse;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public GetMyPointResponse getMyPoint(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return new GetMyPointResponse(member.getId(), member.getPointBalance());
    }

    @Transactional(readOnly = true)
    public PageResponse<PointHistoryResponse> getMyHistory(Long memberId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Point> pointPage = pointRepository.findByMemberId(memberId, pageable);

        List<PointHistoryResponse> content = pointPage.getContent()
                .stream()
                .map(point -> new PointHistoryResponse(
                        point.getId(),
                        point.getPaymentId(),
                        point.getPointType(),
                        point.getAmount(),
                        point.getReason(),
                        point.getCreatedAt()
                ))
                .toList();

        return new PageResponse<>(
                content,
                pointPage.getNumber() + 1,
                pointPage.getSize(),
                pointPage.getTotalElements(),
                pointPage.getTotalPages()
        );
    }

    // 회원 스냅샷 잔액과 포인트 원장 합계가 같은지 검증합니다.
    @Transactional(readOnly = true)
    public boolean isPointBalanceConsistent(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Long ledgerBalance = pointRepository.calculateLedgerBalance(memberId);

        return member.getPointBalance().equals(ledgerBalance);
    }

    @Transactional(readOnly = true)
    public void validatePointBalanceConsistency(Long memberId) {
        if (!isPointBalanceConsistent(memberId)) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }
    }

    // 결제 완료 후 적립 포인트를 지급합니다.
    @Transactional
    public void earnPoints(Payment payment) {
        validatePayment(payment);

        Long amount = payment.getOrder().getEarnedPointAmount();
        if (amount == 0L) {
            return;
        }

        String key = Point.paymentKey(payment.getId(), PointType.EARN);
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        Member member = findMember(payment.getOrder().getMember().getId());
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        member.increasePoint(amount);
        savePoint(member, payment, PointType.EARN, amount, "주문 적립", key);
    }


    // 결제 완료 후 포인트를 차감하고 사용 원장을 기록합니다.
    @Transactional
    public void usePoints(Payment payment) {
        validatePayment(payment);

        Long amount = payment.getOrder().getUsedPointAmount();
        if (amount == 0L) {
            return;
        }

        String key = Point.paymentKey(payment.getId(), PointType.USE);
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        Member member = findMember(payment.getOrder().getMember().getId());
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }
        if (member.getPointBalance() < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }

        member.decreasePoint(amount);
        savePoint(member, payment, PointType.USE, -amount, "주문 사용", key);
    }


    // 환불 시 사용 포인트를 복구하고 멱등키로 중복 복구를 막습니다.
    @Transactional
    public void restoreUsedPoints(Payment payment, Long refundId) {
        validateRefundPointRequest(payment, refundId);

        Long amount = payment.getOrder().getUsedPointAmount();
        if (amount == 0L) {
            return;
        }

        String key = Point.refundKey(refundId, PointType.REFUND);
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        Member member = findMember(payment.getOrder().getMember().getId());
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        member.increasePoint(amount);
        savePoint(member, payment, PointType.REFUND, amount, "사용 포인트 복구", key);
    }


    // 환불 완료 후 지급된 적립 포인트를 회수합니다.
    @Transactional
    public void revokeEarnedPoints(Payment payment, Long refundId) {
        validateRefundPointRequest(payment, refundId);

        Long amount = payment.getOrder().getEarnedPointAmount();
        if (amount == 0L) {
            return;
        }

        String key = Point.refundKey(refundId, PointType.REVOKE);
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        Member member = findMember(payment.getOrder().getMember().getId());
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        member.decreasePoint(amount);
        savePoint(member, payment, PointType.REVOKE, -amount, "적립 포인트 회수", key);
    }

    private void savePoint(
            Member member,
            Payment payment,
            PointType pointType,
            Long amount,
            String reason,
            String idempotencyKey
    ) {
        Point point = new Point(
                member.getId(),
                payment.getId(),
                pointType,
                amount,
                member.getPointBalance(),
                reason,
                idempotencyKey
        );

        pointRepository.save(point);
    }

    private void validateRefundPointRequest(Payment payment, Long refundId) {
        validatePayment(payment);
        if (refundId == null) {
            throw new BusinessException(ErrorCode.INVALID_POINT_REQUEST);
        }
    }

    private void validatePayment(Payment payment) {
        if (payment == null
                || payment.getId() == null
                || payment.getOrder() == null
                || payment.getOrder().getMember() == null
                || payment.getOrder().getMember().getId() == null
                || payment.getOrder().getUsedPointAmount() == null
                || payment.getOrder().getEarnedPointAmount() == null) {
            throw new BusinessException(ErrorCode.INVALID_POINT_REQUEST);
        }

        if (payment.getOrder().getUsedPointAmount() < 0
                || payment.getOrder().getEarnedPointAmount() < 0) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }
    }

    // 포인트 잔액을 변경하는 동안에는 회원 row에 비관적 락을 겁니다.
    private Member findMember(Long memberId) {
        return memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
