package com.example.commercepaymentsystem.domain.refund.repository;

import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByPayment_Id(Long paymentId);
}
