package com.example.commercepaymentsystem.domain.cart.repository;

import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    boolean existsByCartIdAndProductId(Long cartId, Long productId);

    @EntityGraph(attributePaths = {"product"})
    Page<CartItem> findByCartIdAndDeletedFalse(Long cartId, Pageable pageable);
}
