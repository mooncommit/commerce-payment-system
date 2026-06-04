package com.example.commercepaymentsystem.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateMemberRequest {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "형식에 맞게 입력해주세요")
    private String email;
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8)
    private String password;
    @NotBlank(message = "이름을 입력해주세요")
    private String name;
    @NotBlank(message = "전화번호를 입력해주세요")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$")
    private String phoneNumber;

}
