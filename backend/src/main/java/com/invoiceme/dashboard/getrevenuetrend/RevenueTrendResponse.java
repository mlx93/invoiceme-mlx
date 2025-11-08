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
    private List<RevenueDataPoint> dataPoints;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataPoint {
        private LocalDate period;
        private Money revenue;
        private Integer invoiceCount;
    }
}

