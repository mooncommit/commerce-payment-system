package com.example.commercepaymentsystem.domain.cart.service;

import com.example.commercepaymentsystem.domain.cart.dto.CartItemResponse;
import com.example.commercepaymentsystem.domain.cart.entity.Cart;
import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.cart.repository.CartItemRepository;
import com.example.commercepaymentsystem.domain.cart.repository.CartRepository;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.enums.SaleStatus;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    // 장바구니 상품 추가
    @Transactional
    public void addItem(Long memberId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_CART_QUANTITY);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(Cart.builder().member(member).build()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getSaleStatus() != SaleStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.PRODUCT_UNAVAILABLE);
        }

        cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .ifPresentOrElse(
                        cartItem -> cartItem.addQuantity(quantity),
                        () -> cartItemRepository.save(CartItem.builder()
                                .cart(cart)
                                .product(product)
                                .quantity(quantity)
                                .build())
                );
    }

    // 장바구니 상품 수량 변경
    @Transactional
    public void updateQuantity(Long memberId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_CART_QUANTITY);
        }

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        cartItem.updateQuantity(quantity);
    }

    // 장바구니 상품 개별 삭제
    @Transactional
    public void removeItem(Long memberId, Long productId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        if (cartItem.isDeleted()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartItem.delete();
    }

    // 결제용 상품 조회 예시
    public List<CartItem> getSelectedItems(Long memberId, List<Long> cartItemIds) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));

        return cart.getCartItems().stream()
                .filter(item -> cartItemIds.contains(item.getId()))
                .filter(item -> !item.isDeleted())
                .toList();
    }

    // 장바구니 상품 목록을 조회
    @Transactional(readOnly = true)
    public Page<CartItemResponse> getCartItems(Long memberId, Pageable pageable) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));

        // 내부 클래스인 CartItemResponse.from을 호출하도록 맞춤
        return cartItemRepository.findByCartIdAndDeletedFalse(cart.getId(), pageable)
                .map(CartItemResponse::from);
    }
}