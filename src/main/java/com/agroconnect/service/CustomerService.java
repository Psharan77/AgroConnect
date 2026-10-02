package com.agroconnect.service;
import com.agroconnect.dto.CustomerDto;
import com.agroconnect.dto.OrderDto;
import java.util.List;

public interface CustomerService {
    CustomerDto createCustomer(CustomerDto dto);
    CustomerDto getCustomerById(Long id);
    CustomerDto updateCustomer(Long id, CustomerDto dto);
    void deleteCustomer(Long id);
    List<OrderDto> getCustomerOrders(Long id);
}
