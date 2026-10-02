package com.agroconnect.service;

import com.agroconnect.dto.FarmerDashboardDto;

public interface FarmerDashboardService {
    FarmerDashboardDto getDashboardStats(Long farmerId);
}
