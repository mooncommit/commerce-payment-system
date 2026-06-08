package com.example.commercepaymentsystem.domain.point.service;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.point.dto.GetMyPointResponse;
import com.example.commercepaymentsystem.domain.point.dto.PointHistoryResponse;
import com.example.commercepaymentsystem.domain.point.entity.Point;
import com.example.commercepaymentsystem.domain.point.enums.PointType;
import com.example.commercepaymentsystem.domain.point.repository.PointRepository;
import com.example.commercepaymentsystem.global.dto.PageResponse;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    void 내_포인트_조회시_현재_잔액을_반환한다() {
        // given
        Long memberId = 1L;
        Member member = member(memberId, 5000L);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        // when
        GetMyPointResponse response = pointService.getMyPoint(memberId);
        // then
        assertThat(response.getPointBalance()).isEqualTo(5000L);
    }

    @Test
    void 내_포인트_조회시_회원을_찾지못하면_예외를_던진다() {
        // given
        Long memberId = 999L;

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> pointService.getMyPoint(memberId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 포인트_내역_조회시_페이징된_포인트_원장을_반환한다() {
        // given
        Long memberId = 1L;
        int page = 1;
        int size = 10;

        Point point = new Point(
                memberId,
                10L,
                PointType.EARN,
                1000L,
                5000L,
                "주문 적립",
                Point.paymentKey(10L, PointType.EARN)
        );
        ReflectionTestUtils.setField(point, "id", 1L);

        Page<Point> pointPage = new PageImpl<>(List.of(point), PageRequest.of(0, size), 1);
        given(pointRepository.findByMemberId(eq(memberId), any(Pageable.class))).willReturn(pointPage);
        // when
        PageResponse<PointHistoryResponse> response = pointService.getMyHistory(memberId, page, size);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(response.getContent().get(0).getPaymentId()).isEqualTo(10L);
        assertThat(response.getContent().get(0).getType()).isEqualTo(PointType.EARN);
        assertThat(response.getContent().get(0).getAmount()).isEqualTo(1000L);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void 포인트_스냅샷과_원장합계가_같으면_일관성_검증에_성공한다() {
        //given
        Long memberId = 1L;
        Member member = member(memberId, 5000L);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(pointRepository.calculateLedgerBalance(memberId)).willReturn(5000L);
        // when
        boolean consistent = pointService.isPointBalanceConsistent(memberId);
        // then
        assertThat(consistent).isTrue();
    }

    @Test
    void 포인트_스냅샷과_원장합계가_다르면_일관성_검증시_예외를_던진다() {
        Long memberId = 1L;
        Member member = member(memberId, 5000L);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(pointRepository.calculateLedgerBalance(memberId)).willReturn(4700L);

        assertThatThrownBy(() -> pointService.validatePointBalanceConsistency(memberId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 포인트_사용시_잔액을_차감하고_사용_원장을_저장한다() {
        Long memberId = 1L;
        Member member = member(memberId, 5000L);
        Payment payment = paymentWithOrder(10L, member, 3000L, 0L);

        given(memberRepository.findByIdForUpdate(memberId)).willReturn(Optional.of(member));

        pointService.usePoints(payment);

        assertThat(member.getPointBalance()).isEqualTo(2000L);
        then(pointRepository).should().save(argThat(point ->
                point.getMemberId().equals(memberId)
                        && point.getPaymentId().equals(10L)
                        && point.getPointType() == PointType.USE
                        && point.getAmount().equals(-3000L)
                        && point.getBalanceAfter().equals(2000L)
                        && point.getIdempotencyKey().equals(Point.paymentKey(10L, PointType.USE))
        ));
    }

    @Test
    void 포인트_사용시_멱등키가_이미_존재하면_아무것도_하지않는다() {
        Long memberId = 1L;
        Member member = member(memberId, 5000L);
        Payment payment = paymentWithOrder(10L, member, 3000L, 0L);

        given(pointRepository.existsByIdempotencyKey(Point.paymentKey(10L, PointType.USE))).willReturn(true);

        pointService.usePoints(payment);

        assertThat(member.getPointBalance()).isEqualTo(5000L);
        then(memberRepository).should(never()).findByIdForUpdate(any());
        then(pointRepository).should(never()).save(any(Point.class));
    }

    @Test
    void 포인트_적립시_잔액을_증가시키고_적립_원장을_저장한다() {
        Long memberId = 1L;
        Member member = member(memberId, 5000L);
        Payment payment = paymentWithOrder(10L, member, 0L, 300L);

        given(memberRepository.findByIdForUpdate(memberId)).willReturn(Optional.of(member));

        pointService.earnPoints(payment);

        assertThat(member.getPointBalance()).isEqualTo(5300L);
        then(pointRepository).should().save(argThat(point ->
                point.getMemberId().equals(memberId)
                        && point.getPaymentId().equals(10L)
                        && point.getPointType() == PointType.EARN
                        && point.getAmount().equals(300L)
                        && point.getBalanceAfter().equals(5300L)
                        && point.getIdempotencyKey().equals(Point.paymentKey(10L, PointType.EARN))
        ));
    }

    @Test
    void 포인트_사용시_잔액이_부족하면_예외를_던진다() {
        Long memberId = 1L;
        Member member = member(memberId, 1000L);
        Payment payment = paymentWithOrder(10L, member, 3000L, 0L);

        given(memberRepository.findByIdForUpdate(memberId)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> pointService.usePoints(payment))
                .isInstanceOf(BusinessException.class);

        assertThat(member.getPointBalance()).isEqualTo(1000L);
        then(pointRepository).should(never()).save(any(Point.class));
    }

    @Test
    void 사용_포인트_복원시_환불_멱등키로_환불_원장을_저장한다() {
        Long memberId = 1L;
        Long refundId = 20L;
        Member member = member(memberId, 2000L);
        Payment payment = paymentWithOrder(10L, member, 3000L, 300L);

        given(memberRepository.findByIdForUpdate(memberId)).willReturn(Optional.of(member));

        pointService.restoreUsedPoints(payment, refundId);

        assertThat(member.getPointBalance()).isEqualTo(5000L);
        then(pointRepository).should().save(argThat(point ->
                point.getMemberId().equals(memberId)
                        && point.getPaymentId().equals(10L)
                        && point.getPointType() == PointType.REFUND
                        && point.getAmount().equals(3000L)
                        && point.getBalanceAfter().equals(5000L)
                        && point.getIdempotencyKey().equals(Point.refundKey(refundId, PointType.REFUND))
        ));
    }

    @Test
    void 적립_포인트_회수시_환불_멱등키로_회수_원장을_저장한다() {
        Long memberId = 1L;
        Long refundId = 20L;
        Member member = member(memberId, 5000L);
        Payment payment = paymentWithOrder(10L, member, 3000L, 300L);

        given(memberRepository.findByIdForUpdate(memberId)).willReturn(Optional.of(member));

        pointService.revokeEarnedPoints(payment, refundId);

        assertThat(member.getPointBalance()).isEqualTo(4700L);
        then(pointRepository).should().save(argThat(point ->
                point.getMemberId().equals(memberId)
                        && point.getPaymentId().equals(10L)
                        && point.getPointType() == PointType.REVOKE
                        && point.getAmount().equals(-300L)
                        && point.getBalanceAfter().equals(4700L)
                        && point.getIdempotencyKey().equals(Point.refundKey(refundId, PointType.REVOKE))
        ));
    }

    private Member member(Long memberId, Long pointBalance) {
        Member member = new Member(
                "test@test.com",
                "encodedPassword",
                "test-member",
                "01012345678"
        );
        ReflectionTestUtils.setField(member, "id", memberId);
        ReflectionTestUtils.setField(member, "pointBalance", pointBalance);
        return member;
    }

    private Payment paymentWithOrder(Long paymentId, Member member, Long usedPointAmount, Long earnedPointAmount) {
        Order order = newEntity(Order.class);
        ReflectionTestUtils.setField(order, "member", member);
        ReflectionTestUtils.setField(order, "usedPointAmount", usedPointAmount);
        ReflectionTestUtils.setField(order, "earnedPointAmount", earnedPointAmount);

        Payment payment = newEntity(Payment.class);
        ReflectionTestUtils.setField(payment, "id", paymentId);
        ReflectionTestUtils.setField(payment, "order", order);
        return payment;
    }

    private static <T> T newEntity(Class<T> entityType) {
        try {
            java.lang.reflect.Constructor<T> constructor = entityType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
