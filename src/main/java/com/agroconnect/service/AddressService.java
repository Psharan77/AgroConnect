package com.agroconnect.service;
import com.agroconnect.dto.AddressDto;
import java.util.List;

public interface AddressService {
    AddressDto addAddress(Long customerId, AddressDto dto);
    List<AddressDto> getCustomerAddresses(Long customerId);
    AddressDto updateAddress(Long customerId, Long addressId, AddressDto dto);
    void deleteAddress(Long customerId, Long addressId);
}
