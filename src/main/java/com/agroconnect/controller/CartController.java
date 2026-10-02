package com.agroconnect.controller;

import com.agroconnect.dto.CartDto;
import com.agroconnect.dto.CartItemRequestDto;
import com.agroconnect.security.SecurityUtils;
import com.agroconnect.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(cartService.getCart(securityUtils.getCurrentCustomerId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addProductToCart(@Valid @RequestBody CartItemRequestDto request) {
        return ResponseEntity.ok(cartService.addProductToCart(securityUtils.getCurrentCustomerId(), request));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartDto> updateItemQuantity(
            @PathVariable Long id,
            @Valid @RequestBody CartItemRequestDto request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(securityUtils.getCurrentCustomerId(), id, request));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartDto> removeCartItem(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.removeCartItem(securityUtils.getCurrentCustomerId(), id));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart() {
        cartService.clearCart(securityUtils.getCurrentCustomerId());
        return ResponseEntity.ok("Cart cleared successfully.");
    }
}
