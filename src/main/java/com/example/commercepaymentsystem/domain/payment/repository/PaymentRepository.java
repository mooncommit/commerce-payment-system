package com.example.commercepaymentsystem.domain.payment.repository;

import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}