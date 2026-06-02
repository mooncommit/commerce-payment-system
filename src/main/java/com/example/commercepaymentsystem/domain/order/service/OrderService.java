package com.example.commercepaymentsystem.domain.order.service;

import com.example.commercepaymentsystem.domain.order.dto.response.PreviewOrderResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    public PreviewOrderResponse preview(List<Long> cartItemIds) {
        return new PreviewOrderResponse(List.of(), 0L, 0L);
    }
}
