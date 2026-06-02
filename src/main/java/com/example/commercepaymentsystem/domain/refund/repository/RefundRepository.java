package com.example.commercepaymentsystem.domain.refund.repository;

import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}