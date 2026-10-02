package com.agroconnect.service.impl;

import com.agroconnect.dto.JwtAuthResponse;
import com.agroconnect.dto.LoginDto;
import com.agroconnect.dto.RegisterDto;
import com.agroconnect.entity.Role;
import com.agroconnect.entity.User;
import com.agroconnect.exception.BadRequestException;
import com.agroconnect.repository.UserRepository;
import com.agroconnect.repository.CustomerRepository;
import com.agroconnect.repository.FarmerRepository;
import com.agroconnect.security.JwtTokenProvider;
import com.agroconnect.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String register(RegisterDto registerDto) {

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new BadRequestException("Email is already registered.");
        }

        User user = new User();
        user.setFullName(registerDto.getFullName());
        user.setEmail(registerDto.getEmail());
        
        // Hashing the password before saving to DB
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        try {
            Role role = Role.valueOf(registerDto.getRole().toUpperCase());
            user.setRole(role);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Must be CUSTOMER, FARMER, or ADMIN.");
        }

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.CUSTOMER) {
            com.agroconnect.entity.Customer customer = new com.agroconnect.entity.Customer();
            customer.setUser(savedUser);
            customerRepository.save(customer);
        } else if (savedUser.getRole() == Role.FARMER) {
            com.agroconnect.entity.Farmer farmer = new com.agroconnect.entity.Farmer();
            farmer.setUser(savedUser);
            farmer.setFarmName(savedUser.getFullName() + "'s Farm");
            farmerRepository.save(farmer);
        }

        return "User registered successfully.";
    }

    @Override
    public JwtAuthResponse login(LoginDto loginDto) {
        // Authenticates via Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT Token
        String token = jwtTokenProvider.generateToken(authentication);
        
        User user = userRepository.findByEmail(loginDto.getEmail()).get();

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);
        jwtAuthResponse.setRole(user.getRole().name());
        jwtAuthResponse.setUserId(user.getId());

        return jwtAuthResponse;
    }
}
