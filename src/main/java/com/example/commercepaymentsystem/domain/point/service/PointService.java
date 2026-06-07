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

    //────────────────────────────────────내 포인트 조회────────────────────────────────────
    @Transactional(readOnly = true)
    public GetMyPointResponse getMyPoint(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return new GetMyPointResponse(member.getId(),member.getPointBalance());
    }

    //────────────────────────────────────포인트 내역────────────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<PointHistoryResponse> getMyHistory(Long memberId, int page, int size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Point> pointPage =
                pointRepository.findByMemberId(memberId, pageable);


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

    //포인트 적립
    public void earnPoints(Payment payment) {
        Long amount = payment.getOrder().getEarnedPointAmount();

        if (amount == null || amount == 0L) {
            return;
        }

        Member member = findMember(payment.getOrder().getMember().getId());

        member.increasePoint(amount);

        Point point = new Point(
                member.getId(),
                payment.getId(),
                PointType.EARN,
                amount,
                member.getPointBalance(),
                "주문 적립"
        );

        pointRepository.save(point);
    }

    //포인트 사용
    public void usePoints(Payment payment) {
        Long amount = payment.getOrder().getUsedPointAmount();

        if (amount == null || amount == 0L) {
            return;
        }

        Member member = findMember(payment.getOrder().getMember().getId());

        if (member.getPointBalance() < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }

        member.decreasePoint(amount);

        Point point = new Point(
                member.getId(),
                payment.getId(),
                PointType.USE,
                -amount,
                member.getPointBalance(),
                "주문 사용"
        );

        pointRepository.save(point);
    }

    //포인트 복구
    public void restoreUsedPoints(Payment payment) {
        Long amount = payment.getOrder().getUsedPointAmount();

        if (amount == null || amount == 0L) {
            return;
        }

        Member member = findMember(payment.getOrder().getMember().getId());

        member.increasePoint(amount);

        Point point = new Point(
                member.getId(),
                payment.getId(),
                PointType.REFUND,
                amount,
                member.getPointBalance(),
                "사용 포인트 복구"
        );

        pointRepository.save(point);
    }

    //포인트 회수
    public void revokeEarnedPoints(Payment payment) {
        Long amount = payment.getOrder().getEarnedPointAmount();

        if (amount == null || amount == 0L) {
            return;
        }

        Member member = findMember(payment.getOrder().getMember().getId());

        member.decreasePoint(amount);

        Point point = new Point(
                member.getId(),
                payment.getId(),
                PointType.REVOKE,
                amount,
                member.getPointBalance(),
                "적립 포인트 회수"
        );

        pointRepository.save(point);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
