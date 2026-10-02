package com.agroconnect.security;

import com.agroconnect.entity.Customer;
import com.agroconnect.entity.Farmer;
import com.agroconnect.entity.User;
import com.agroconnect.exception.ResourceNotFoundException;
import com.agroconnect.repository.CustomerRepository;
import com.agroconnect.repository.FarmerRepository;
import com.agroconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final FarmerRepository farmerRepository;

    public String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public User getCurrentUser() {
        return userRepository.findByEmail(getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Long getCurrentCustomerId() {
        return customerRepository.findByUserEmail(getCurrentUserEmail())
                .map(Customer::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found. Did you complete registration?"));
    }

    public Long getCurrentFarmerId() {
        return farmerRepository.findByUserEmail(getCurrentUserEmail())
                .map(Farmer::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer profile not found. Did you complete registration?"));
    }
}
