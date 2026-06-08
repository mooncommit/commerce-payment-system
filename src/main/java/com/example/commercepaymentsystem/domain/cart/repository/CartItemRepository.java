package com.example.commercepaymentsystem.domain.cart.repository;

import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    boolean existsByCartIdAndProductId(Long cartId, Long productId);

    @EntityGraph(attributePaths = {"product"})
    Page<CartItem> findByCartIdAndDeletedFalse(Long cartId, Pageable pageable);

    // OrderService에서 호출하는 메서드 정의
    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT ci FROM CartItem ci JOIN ci.cart c WHERE c.member.id = :memberId AND ci.deleted = false")
    List<CartItem> findAllByMemberIdWithProduct(@Param("memberId") Long memberId);
}
