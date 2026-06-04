package com.example.commercepaymentsystem.domain.auth.entity;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private Long memberId;

    private LocalDateTime expiredAt;

    public RefreshToken(String token, Long memberId, LocalDateTime expiredAt)
    {
        this.token = token;
        this.memberId = memberId;
        this.expiredAt = expiredAt;
    }
}
