package com.example.commercepaymentsystem.domain.cart.service;

import com.example.commercepaymentsystem.domain.cart.dto.CartResponse;
import com.example.commercepaymentsystem.domain.cart.dto.CartResponse.CartItemResponse;
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

    public CartResponse getCart(Long memberId) {

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));


        if (cart.getCartItems().isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        List<CartItemResponse> itemResponses = cart.getCartItems().stream()
                .map(item -> CartItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .price(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .build();
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

        // 장바구니에 있는 아이템 중, 주문할 아이템 ID 목록에 해당하는 것만 필터링
        return cart.getCartItems().stream()
                .filter(item -> cartItemIds.contains(item.getId()))
                .filter(item -> !item.isDeleted())
                .toList();
    }


}