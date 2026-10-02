package com.agroconnect.service.impl;

import com.agroconnect.dto.CartDto;
import com.agroconnect.dto.CartItemDto;
import com.agroconnect.dto.CartItemRequestDto;
import com.agroconnect.entity.Cart;
import com.agroconnect.entity.CartItem;
import com.agroconnect.entity.Customer;
import com.agroconnect.entity.Product;
import com.agroconnect.exception.BadRequestException;
import com.agroconnect.exception.ResourceNotFoundException;
import com.agroconnect.repository.CartItemRepository;
import com.agroconnect.repository.CartRepository;
import com.agroconnect.repository.CustomerRepository;
import com.agroconnect.repository.ProductRepository;
import com.agroconnect.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    // Helper to fetch or create a cart for a user
    private Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId).orElseGet(() -> {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            Cart newCart = new Cart();
            newCart.setCustomer(customer);
            return cartRepository.save(newCart);
        });
    }

    @Override
    @Transactional
    public CartDto getCart(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        return mapToDto(cart);
    }

    @Override
    @Transactional
    public CartDto addProductToCart(Long customerId, CartItemRequestDto request) {
        Cart cart = getOrCreateCart(customerId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (request.getQuantity() > product.getQuantity()) {
            throw new BadRequestException("Requested quantity exceeds available stock (" + product.getQuantity() + ")");
        }

        // Check if product already exists in cart
        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > product.getQuantity()) {
                throw new BadRequestException("Total quantity exceeds available stock");
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return mapToDto(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartDto updateItemQuantity(Long customerId, Long itemId, CartItemRequestDto request) {
        Cart cart = getOrCreateCart(customerId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to your cart");
        }

        if (request.getQuantity() > item.getProduct().getQuantity()) {
            throw new BadRequestException("Requested quantity exceeds available stock");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return mapToDto(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartDto removeCartItem(Long customerId, Long itemId) {
        Cart cart = getOrCreateCart(customerId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to your cart");
        }

        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);
        return mapToDto(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    private CartDto mapToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setCustomerId(cart.getCustomer().getId());

        List<CartItemDto> itemDtos = cart.getCartItems().stream().map(item -> {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setProductPrice(item.getProduct().getPrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setSubTotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            return itemDto;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);

        BigDecimal total = itemDtos.stream()
                .map(CartItemDto::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalPrice(total);

        return dto;
    }
}
