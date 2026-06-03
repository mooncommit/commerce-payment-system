package com.example.commercepaymentsystem.domain.auth.service;

import com.example.commercepaymentsystem.domain.auth.dto.*;
import com.example.commercepaymentsystem.domain.auth.entity.RefreshToken;
import com.example.commercepaymentsystem.domain.auth.jwt.JwtProvider;
import com.example.commercepaymentsystem.domain.auth.repository.RefreshTokenRepository;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.global.config.PasswordEncoder;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public CreateMemberResponse signupMember(CreateMemberRequest request) {

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

    @Transactional
    public LoginResponse login(LoginRequest request) {

        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_MISMATCH));

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) { //아닌 matches 로 비교
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getEmail());

        refreshTokenRepository.deleteByMemberId(member.getId()); // 기존토큰삭제

        refreshTokenRepository.save(
                new RefreshToken(refreshToken,
                        member.getId(),
                        jwtProvider.getRefreshTokenExpiryDateTime()
                )
        );
        return new LoginResponse(member.getId(), member.getEmail(), member.getName(), accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissue(TokenRequest request) {

        String refreshToken = request.getRefreshToken();

        jwtProvider.validateToken(refreshToken);

        RefreshToken savedToken =
                refreshTokenRepository.findByToken(refreshToken)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        Long memberId = savedToken.getMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken =
                jwtProvider.createAccessToken(
                        member.getId(),
                        member.getEmail()
                );

        return new TokenResponse(newAccessToken);
    }

//    @Transactional
//    public void logout(Long memberId) {
//
//        RefreshToken refreshToken =
//                refreshTokenRepository
//                        .findByMemberId(memberId)
//                        .orElseThrow(...);
//
//        refreshTokenRepository.delete(refreshToken);
//    }
}
