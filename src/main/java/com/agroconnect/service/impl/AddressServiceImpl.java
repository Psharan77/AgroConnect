package com.agroconnect.service.impl;

import com.agroconnect.dto.AddressDto;
import com.agroconnect.entity.Address;
import com.agroconnect.entity.Customer;
import com.agroconnect.exception.ResourceNotFoundException;
import com.agroconnect.repository.AddressRepository;
import com.agroconnect.repository.CustomerRepository;
import com.agroconnect.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    @Override
    public AddressDto addAddress(Long customerId, AddressDto dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Address address = new Address();
        address.setCustomer(customer);
        address.setFullName(dto.getFullName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);

        return mapToDto(addressRepository.save(address));
    }

    @Override
    public List<AddressDto> getCustomerAddresses(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
                
        return customer.getAddresses().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public AddressDto updateAddress(Long customerId, Long addressId, AddressDto dto) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw new com.agroconnect.exception.BadRequestException("Cannot modify another customer's address");
        }
        
        address.setFullName(dto.getFullName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setIsDefault(dto.getIsDefault());

        return mapToDto(addressRepository.save(address));
    }

    @Override
    public void deleteAddress(Long customerId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
                
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new com.agroconnect.exception.BadRequestException("Cannot delete another customer's address");
        }
        
        addressRepository.delete(address);
    }

    private AddressDto mapToDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setCustomerId(address.getCustomer().getId());
        dto.setFullName(address.getFullName());
        dto.setPhoneNumber(address.getPhoneNumber());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }
}
