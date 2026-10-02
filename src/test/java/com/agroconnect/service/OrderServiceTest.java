package com.agroconnect.service;

import com.agroconnect.dto.OrderDto;
import com.agroconnect.dto.OrderRequestDto;
import com.agroconnect.entity.*;
import com.agroconnect.exception.BadRequestException;
import com.agroconnect.repository.*;
import com.agroconnect.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer customer;
    private Cart cart;
    private Address address;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);

        address = new Address();
        address.setId(1L);
        address.setCustomer(customer);

        product = new Product();
        product.setId(1L);
        product.setName("Organic Apples");
        product.setQuantity(10); // Initial stock is 10
        product.setPrice(new BigDecimal("2.00"));

        cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(5); // Customer wants to buy 5

        cart = new Cart();
        cart.setId(1L);
        cart.setCustomer(customer);
        cart.getCartItems().add(cartItem);
    }

    @Test
    void testCreateOrder_StockReducedSuccessfully() {
        // GIVEN: A valid cart and address
        OrderRequestDto request = new OrderRequestDto();
        request.setAddressId(1L);

        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        
        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setCustomer(customer);
        savedOrder.setAddress(address);
        savedOrder.setTotalAmount(new BigDecimal("10.00"));
        savedOrder.setStatus("PLACED");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // WHEN: The customer attempts to checkout
        OrderDto result = orderService.createOrder(1L, request);

        // THEN: The order is created successfully
        assertNotNull(result);
        assertEquals("PLACED", result.getStatus());
        
        // THEN: Verify the stock was reduced exactly by the cart amount (10 - 5 = 5)
        assertEquals(5, product.getQuantity());
        verify(productRepository, times(1)).save(product);
        
        // THEN: Verify the customer's cart was cleared after purchase
        verify(cartItemRepository, times(1)).deleteAll(anyList());
    }

    @Test
    void testCreateOrder_ThrowsExceptionWhenStockInsufficient() {
        // GIVEN: A customer trying to buy MORE than what the farmer has
        OrderRequestDto request = new OrderRequestDto();
        request.setAddressId(1L);
        
        cartItem.setQuantity(15); // Wants 15, but only 10 exist!

        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        // WHEN & THEN: The system should throw a BadRequestException and HALT the transaction
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            orderService.createOrder(1L, request);
        });
        
        assertTrue(exception.getMessage().contains("does not have enough stock"));

        // THEN: Verify stock was NEVER reduced and cart was NEVER cleared
        assertEquals(10, product.getQuantity());
        verify(productRepository, never()).save(any(Product.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
    }
}
