package com.example.commercepaymentsystem.domain.point.repository;

import com.example.commercepaymentsystem.domain.point.entity.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointRepository extends JpaRepository<Point, Long> {

    Page<Point> findByMemberId(Long memberId, Pageable pageable);

    boolean existsByIdempotencyKey(String idempotencyKey);

    /*
     * 적립/복구는 양수, 사용/회수는 음수로 저장합니다.
     * 따라서 단순 합계로 원장 기준 잔액을 계산할 수 있습니다.
     */
    @Query("""
            select coalesce(sum(p.amount), 0)
            from Point p
            where p.memberId = :memberId
            """)
    Long calculateLedgerBalance(@Param("memberId") Long memberId);
}
