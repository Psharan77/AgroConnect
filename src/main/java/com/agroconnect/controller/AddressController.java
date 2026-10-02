package com.agroconnect.controller;

import com.agroconnect.dto.AddressDto;
import com.agroconnect.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final com.agroconnect.security.SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<AddressDto> addAddress(@jakarta.validation.Valid @RequestBody AddressDto dto) {
        return ResponseEntity.ok(addressService.addAddress(securityUtils.getCurrentCustomerId(), dto));
    }

    @GetMapping
    public ResponseEntity<java.util.List<AddressDto>> getCustomerAddresses() {
        return ResponseEntity.ok(addressService.getCustomerAddresses(securityUtils.getCurrentCustomerId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable Long id, @jakarta.validation.Valid @RequestBody AddressDto dto) {
        return ResponseEntity.ok(addressService.updateAddress(securityUtils.getCurrentCustomerId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(securityUtils.getCurrentCustomerId(), id);
        return ResponseEntity.ok("Address deleted successfully.");
    }
}
