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

    @Transactional(readOnly = true)
    public boolean isPointBalanceConsistent(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Long ledgerBalance = pointRepository.calculateLedgerBalance(memberId);

        return member.getPointBalance().equals(ledgerBalance);
    }


    //잔액과 스냅샷이 다를시 예외발생
    @Transactional(readOnly = true)
    public void validatePointBalanceConsistency(Long memberId) {
        if (!isPointBalanceConsistent(memberId)) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }
    }


    //결제완료후 포인트지급
    @Transactional
    public void earnPoints(Payment payment) {
        validatePaymentPointRequest(payment);

        Long amount = payment.getOrder().getEarnedPointAmount();
        if (amount == 0L) {
            return;
        }

        String key = Point.paymentKey(payment.getId(), PointType.EARN);
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        Member member = findMemberForPointUpdate(payment);
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        member.increasePoint(amount);
        savePoint(member, payment, PointType.EARN, amount, "주문 적립", key);
    }


    //결제완료후 사용포인트차감
    @Transactional
    public void usePoints(Payment payment) {
        validatePaymentPointRequest(payment);

        Long amount = payment.getOrder().getUsedPointAmount();
        if (amount == 0L) {
            return;
        }

        String key = Point.paymentKey(payment.getId(), PointType.USE);
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        Member member = findMemberForPointUpdate(payment);
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }
        if (member.getPointBalance() < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }

        member.decreasePoint(amount);
        savePoint(member, payment, PointType.USE, -amount, "주문 사용", key);
    }


    //환불포인트복구
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

        Member member = findMemberForPointUpdate(payment);
        if (pointRepository.existsByIdempotencyKey(key)) {
            return;
        }

        member.increasePoint(amount);
        savePoint(member, payment, PointType.REFUND, amount, "사용 포인트 복구", key);
    }


    //환불포인트회수
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

        Member member = findMemberForPointUpdate(payment);
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

    //환불처리에 포인트값이 유효한지확인
    private void validateRefundPointRequest(Payment payment, Long refundId) {
        validatePaymentPointRequest(payment);
        if (refundId == null) {
            throw new BusinessException(ErrorCode.INVALID_POINT_REQUEST);
        }
    }


    //결제 기반 포인트 처리에 필요한 스냅샷 값이 유효한지 검증
    private void validatePaymentPointRequest(Payment payment) {
        if (payment == null
                || payment.getId() == null
                || payment.getOrder() == null
                || payment.getOrder().getMember() == null
                || payment.getOrder().getMember().getId() == null
                || payment.getOrder().getUsedPointAmount() == null
                || payment.getOrder().getEarnedPointAmount() == null) {
            throw new BusinessException(ErrorCode.INVALID_POINT_REQUEST);
        }

        validatePointAmount(payment.getOrder().getUsedPointAmount());
        validatePointAmount(payment.getOrder().getEarnedPointAmount());
    }


    //포인트 스냅샷금액은 음수로 될수없다
    private void validatePointAmount(Long amount) {
        if (amount < 0) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }
    }


    //한건만 진행할수있도록 비관락적용
    private Member findMemberForPointUpdate(Payment payment) {
        Long memberId = payment.getOrder().getMember().getId();
        return memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
