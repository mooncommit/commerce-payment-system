package com.example.commercepaymentsystem.domain.product.repository;

import com.example.commercepaymentsystem.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.commercepaymentsystem.domain.product.enums.SaleStatus;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.categoryCode = :category) AND " +
           "(:status IS NULL OR p.saleStatus = :status)")
    Page<Product> findAllByFilters(@Param("category") String category,
                                   @Param("status") SaleStatus status,
                                   Pageable pageable);
}
