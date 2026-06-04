package com.example.commercepaymentsystem.domain.point.repository;

import com.example.commercepaymentsystem.domain.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointRepository extends JpaRepository<Point,Long> {
    //전체 거래 내역을 최신순으로 조회
    List<Point> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
