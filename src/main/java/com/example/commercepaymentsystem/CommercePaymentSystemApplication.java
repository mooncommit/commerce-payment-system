package com.example.commercepaymentsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
//스케쥴링
@EnableScheduling
public class CommercePaymentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommercePaymentSystemApplication.class, args);
    }

}



