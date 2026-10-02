package com.agroconnect.controller;

import com.agroconnect.dto.AddressDto;
import com.agroconnect.dto.CustomerDto;
import com.agroconnect.dto.OrderDto;
import com.agroconnect.service.AddressService;
import com.agroconnect.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final AddressService addressService;
    private final com.agroconnect.security.SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody CustomerDto dto) {
        dto.setUserId(securityUtils.getCurrentUser().getId());
        return new ResponseEntity<>(customerService.createCustomer(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id, @RequestBody CustomerDto dto) {
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok("Customer deleted successfully.");
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderDto>> getCustomerOrders(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerOrders(id));
    }

    // Address Endpoints grouped under Customer mapping where applicable
    @PostMapping("/{id}/addresses")
    public ResponseEntity<AddressDto> addAddress(@PathVariable Long id, @RequestBody AddressDto dto) {
        return new ResponseEntity<>(addressService.addAddress(id, dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/addresses")
    public ResponseEntity<List<AddressDto>> getCustomerAddresses(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getCustomerAddresses(id));
    }
}
