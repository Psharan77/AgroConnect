package com.agroconnect.controller;

import com.agroconnect.dto.FarmerDashboardDto;
import com.agroconnect.security.SecurityUtils;
import com.agroconnect.service.FarmerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/farmer/dashboard")
@RequiredArgsConstructor
public class FarmerDashboardController {

    private final FarmerDashboardService farmerDashboardService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<FarmerDashboardDto> getDashboardStats() {
        return ResponseEntity.ok(farmerDashboardService.getDashboardStats(securityUtils.getCurrentFarmerId()));
    }
}
