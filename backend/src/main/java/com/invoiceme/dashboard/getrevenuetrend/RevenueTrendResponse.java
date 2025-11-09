package com.invoiceme.dashboard.getrevenuetrend;

import com.invoiceme.domain.common.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendResponse {
    private List<RevenueTrendData> data; // Renamed from dataPoints to match frontend
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueTrendData {
        private String month; // Changed from period (LocalDate) to month (String) to match frontend
        private Money revenue;
    }
}

