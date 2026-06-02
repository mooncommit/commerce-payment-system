package com.example.commercepaymentsystem.domain.order.repository;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
