package com.example.commercepaymentsystem.domain.auth.jwt;

import com.example.commercepaymentsystem.global.exception.ErrorCode;
import com.example.commercepaymentsystem.global.exception.JwtTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.nio.charset.StandardCharsets; // StandardCharsets 임포트 추가

@Slf4j
@Component
public class JwtProvider {

    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;
    private final SecretKey key;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access.token.expiration}") long accessTokenExpirationTime,
            @Value("${jwt.refresh.token.expiration}") long refreshTokenExpirationTime
    ) {
        this.key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8) // secretKey 문자열 → JWT 서명용 SecretKey 객체 변환
        );
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    // Access Token 생성 메서드
    // 사용자 ID와 이메일을 클레임(payload)에 담아 짧은 유효 시간으로 토큰을 생성한다.
    public String createAccessToken(Long memberId, String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenExpirationTime); // 현재 시간 + 유효 시간

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("email", email) // 클레임 설정
                .issuedAt(now) // 토큰 발행 시간
                .expiration(validity) // 토큰 만료 시간
                .signWith(key) // HS256 알고리즘으로 서명
                .compact(); // 토큰 생성
    }

    // Refresh Token 생성 메서드
    public String createRefreshToken(Long memberId, String email) {

        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenExpirationTime);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("email",email)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public Long getMemberId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    // 토큰에서 사용자 이메일 추출
    public String getEmail(String token) {
        return getClaims(token).get("email",String.class);
    }

    /* 토큰 유효성 검증
    // 왜 JwtException을 구분하는가?
    // 토큰이 단순히 만료된 것인지(ExpiredJwtException) 아니면 서명이 위조되었거나 형식이 잘못된 것인지
    // 구분하여 적절한 에러 처리를 할 수 있도록 돕는다. 예를 들어, 만료된 토큰은 Refresh Token으로 재발급을 시도할 수 있지만,
    위조된 토큰은 즉시 거부해야 한다.
    */
    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        } catch (SecurityException | MalformedJwtException e) {
            throw new JwtTokenException(ErrorCode.EXPIRED_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException(ErrorCode.INVALID_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new JwtTokenException(ErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new JwtTokenException(ErrorCode.EMPTY_TOKEN);
        }
    }

    // 토큰에서 Claims(페이로드) 추출
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key) //서명 검증 (토큰이 맞는지 확인)
                .build()//parser 생성
                .parseSignedClaims(token)//토큰 해석 , 서명 ,만료시간 검증
                .getPayload();//Payload 반환
    }

    // Refresh Token 만료 시간 반환
    public LocalDateTime getRefreshTokenExpiryDateTime() {
        return LocalDateTime.now().plus(refreshTokenExpirationTime, ChronoUnit.MILLIS);
    }


    // Access Token 만료 시간 반환
    public LocalDateTime getAccessTokenExpiryDateTime() {
        return LocalDateTime.now().plus(accessTokenExpirationTime, ChronoUnit.MILLIS);
    }

}