package com.example.commercepaymentsystem.domain.member.service;

import com.example.commercepaymentsystem.domain.member.dto.CreateMemberRequest;
import com.example.commercepaymentsystem.domain.member.dto.CreateMemberResponse;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.global.config.PasswordEncoder;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CreateMemberResponse signupMember(@Valid CreateMemberRequest request) {

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.CONFLICT_EMAIL);
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword()); //비밀번호암호화

        Member member = new Member(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getPhoneNumber()
        );
        Member savedMember = memberRepository.save(member);

        return new CreateMemberResponse(
                savedMember.getId(),
                savedMember.getEmail(),
                savedMember.getName(),
                savedMember.getPhoneNumber(),
                savedMember.getPointBalance(),
                savedMember.getMemberShip(),
                savedMember.getCreatedAt()
        );


    }
}
