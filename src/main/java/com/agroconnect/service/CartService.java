package com.agroconnect.service;
import com.agroconnect.dto.CartDto;
import com.agroconnect.dto.CartItemRequestDto;

public interface CartService {
    CartDto getCart(Long customerId);
    CartDto addProductToCart(Long customerId, CartItemRequestDto request);
    CartDto updateItemQuantity(Long customerId, Long itemId, CartItemRequestDto request);
    CartDto removeCartItem(Long customerId, Long itemId);
    void clearCart(Long customerId);
}
