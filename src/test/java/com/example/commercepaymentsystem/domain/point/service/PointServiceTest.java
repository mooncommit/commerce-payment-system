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
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    void 포인트_잔액을_조회한다() {
        // given
        Long memberId = 1L;

        Member member = new Member(
                "test@test.com",
                "encodedPassword",
                "테스트회원",
                "01012345678"
        );
        ReflectionTestUtils.setField(member, "id", memberId);
        ReflectionTestUtils.setField(member, "pointBalance", 5000L);

        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(member));

        // when
        GetMyPointResponse response = pointService.getMyPoint(memberId);

        // then
        assertThat(response.getPointBalance()).isEqualTo(5000L);
    }

    @Test
    void 존재하지_않는_회원이면_예외가_발생한다() {
        // given
        Long memberId = 999L;

        given(memberRepository.findById(memberId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.getMyPoint(memberId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 포인트_거래내역을_페이징_조회한다() {
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
                "주문 적립"
        );
        ReflectionTestUtils.setField(point, "id", 1L);

        Page<Point> pointPage =
                new PageImpl<>(List.of(point), PageRequest.of(0, size), 1);

        given(pointRepository.findByMemberId(eq(memberId), any(Pageable.class)))
                .willReturn(pointPage);

        // when
        PageResponse<PointHistoryResponse> response =
                pointService.getMyHistory(memberId, page, size);

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
    void 포인트를_사용하면_잔액이_감소하고_사용_원장이_저장된다() {
        // given
        Long memberId = 1L;

        Member member = new Member(
                "test@test.com",
                "encodedPassword",
                "테스트회원",
                "01012345678"
        );
        ReflectionTestUtils.setField(member, "id", memberId);
        ReflectionTestUtils.setField(member, "pointBalance", 5000L);

        Payment payment = mock(Payment.class);

        given(payment.getId()).willReturn(10L);
        given(payment.getMemberId()).willReturn(memberId);
        given(payment.getUsedPointAmount()).willReturn(3000L);

        given(memberRepository.findByIdForUpdate(memberId))
                .willReturn(Optional.of(member));

        // when
        pointService.usePoints(payment);

        // then
        assertThat(member.getPointBalance()).isEqualTo(2000L);

        then(pointRepository).should().save(argThat(point ->
                point.getMemberId().equals(memberId)
                        && point.getPaymentId().equals(10L)
                        && point.getPointType() == PointType.USE
                        && point.getAmount().equals(-3000L)
                        && point.getBalanceAfter().equals(2000L)
        ));
    }

    @Test
    void 포인트를_적립하면_잔액이_증가하고_적립_원장이_저장된다() {
        // given
        Long memberId = 1L;

        Member member = new Member(
                "test@test.com",
                "encodedPassword",
                "테스트회원",
                "01012345678"
        );
        ReflectionTestUtils.setField(member, "id", memberId);
        ReflectionTestUtils.setField(member, "pointBalance", 5000L);

        Payment payment = mock(Payment.class);

        given(payment.getId()).willReturn(10L);
        given(payment.getMemberId()).willReturn(memberId);
        given(payment.getEarnedPointAmount()).willReturn(300L);

        given(memberRepository.findByIdForUpdate(memberId))
                .willReturn(Optional.of(member));

        // when
        pointService.earnPoints(payment);

        // then
        assertThat(member.getPointBalance()).isEqualTo(5300L);

        then(pointRepository).should().save(argThat(point ->
                point.getMemberId().equals(memberId)
                        && point.getPaymentId().equals(10L)
                        && point.getPointType() == PointType.EARN
                        && point.getAmount().equals(300L)
                        && point.getBalanceAfter().equals(5300L)
        ));
    }

    @Test
    void 보유_포인트보다_많이_사용하면_예외가_발생하고_원장은_저장되지_않는다() {
        // given
        Long memberId = 1L;

        Member member = new Member(
                "test@test.com",
                "encodedPassword",
                "테스트회원",
                "01012345678"
        );
        ReflectionTestUtils.setField(member, "id", memberId);
        ReflectionTestUtils.setField(member, "pointBalance", 1000L);

        Payment payment = mock(Payment.class);

        given(payment.getMemberId()).willReturn(memberId);
        given(payment.getUsedPointAmount()).willReturn(3000L);

        given(memberRepository.findByIdForUpdate(memberId))
                .willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> pointService.usePoints(payment))
                .isInstanceOf(BusinessException.class);

        assertThat(member.getPointBalance()).isEqualTo(1000L);

        then(pointRepository).should(never()).save(any(Point.class));
    }
}