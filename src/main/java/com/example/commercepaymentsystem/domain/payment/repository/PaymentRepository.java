package com.example.commercepaymentsystem.domain.payment.repository;

import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdAndOrder_Member_Id(Long paymentId, Long memberId);

    Optional<Payment> findByOrder_IdAndPortonePaymentId(Long orderId, String portonePaymentId);

    // 주문 단건 조회 화면에서 결제 ID 한 건만 필요하므로 Payment 엔티티 전체를 로딩하지 않고 ID만 프로젝션
    // Order ← Payment가 단방향 관계(Order가 Payment를 참조하지 않음)라서,
    // 주문에 결제 ID를 붙이려면 이렇게 Payment 쪽에서 역으로 찾아야 한다.
    @Query("SELECT p.id FROM Payment p WHERE p.order.id = :orderId")
    Optional<Long> findIdByOrderId(@Param("orderId") Long orderId);

    // 주문 "목록" 조회용 N+1 방지 일괄 조회
    // 각 주문마다 findIdByOrderId를 돌리면 N번 쿼리가 나가는데, IN 절로 한 번에 가져와
    // 서비스 레이어에서 Map<OrderId, PaymentId>로 재구성!
    // 반환이 List<Object[]>인 이유: [orderId, paymentId] 쌍을 내려받기 위한 JPA 튜플 프로젝션
    // 타입 안전성은 떨어지므로, 실무에선 인터페이스/레코드 프로젝션으로 개선 가능
    @Query("SELECT p.order.id, p.id FROM Payment p WHERE p.order.id IN :orderIds")
    List<Object[]> findIdsByOrderIds(@Param("orderIds") List<Long> orderIds);

    // 주문 단건 상세 조회 : orderId만으로 조회
    @Query("SELECT p FROM Payment p JOIN FETCH p.order o WHERE p.order.id = :orderId")
    Optional<Payment> findByOrderIdWithOrder(@Param("orderId") Long orderId);

    // Payment 조회 시 환불 권한 검증에 필요한 Order와 Member를 함께 로딩한다.
    @Query("SELECT p FROM Payment p JOIN FETCH p.order o JOIN FETCH o.member WHERE p.id = :paymentId")
    Optional<Payment> findByIdWithOrder(@Param("paymentId") Long paymentId);

    // 웹훅 처리용: 포트원 결제 고유번호(imp_uid)를 통해 결제와 주문, 회원을 모두 로딩한다.
    @Query("SELECT p FROM Payment p JOIN FETCH p.order o JOIN FETCH o.member WHERE p.portonePaymentId = :portonePaymentId")
    Optional<Payment> findByPortonePaymentIdWithOrderAndMember(@Param("portonePaymentId") String portonePaymentId);
}
