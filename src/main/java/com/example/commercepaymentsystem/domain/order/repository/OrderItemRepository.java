package com.example.commercepaymentsystem.domain.order.repository;

import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
