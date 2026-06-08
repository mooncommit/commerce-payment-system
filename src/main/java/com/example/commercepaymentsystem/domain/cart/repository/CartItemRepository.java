package com.example.commercepaymentsystem.domain.cart.repository;

import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 주문서 미리보기에서 로그인 회원의 장바구니 상품과 상품 정보를 함께 조회
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product p JOIN FETCH ci.cart c WHERE c.member.id = :memberId")
    List<CartItem> findAllByMemberIdWithProduct(@Param("memberId") Long memberId);
}
