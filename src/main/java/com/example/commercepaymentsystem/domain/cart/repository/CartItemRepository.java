package com.example.commercepaymentsystem.domain.cart.repository;

import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
