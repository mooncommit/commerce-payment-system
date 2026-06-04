package com.example.commercepaymentsystem.domain.point.service;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.domain.point.dto.GetMyPointResponse;
import com.example.commercepaymentsystem.domain.point.repository.PointRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public GetMyPointResponse getMyPoint(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return new GetMyPointResponse(member.getId(),member.getPointBalance());
    }


}
