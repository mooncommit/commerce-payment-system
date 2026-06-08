package com.example.commercepaymentsystem.domain.point.scheduler;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.domain.point.service.PointService;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointReconciliationScheduler {

    private final MemberRepository memberRepository;
    private final PointService pointService;

    /*
     매일 새벽 3시에 회원 포인트 잔액 스냅샷과 포인트 원장 합계를 대사합니다.
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void reconcilePointBalances() {
        for (Member member : memberRepository.findAll()) {
            try {
                pointService.validatePointBalanceConsistency(member.getId());
            } catch (BusinessException e) {
                log.warn(
                        "포인트 잔액 정합성 불일치: memberId={}, errorCode={}",
                        member.getId(),
                        e.getErrorCode().getCode()
                );
            }
        }
    }
}
