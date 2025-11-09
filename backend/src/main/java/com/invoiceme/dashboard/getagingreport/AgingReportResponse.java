package com.invoiceme.dashboard.getagingreport;

import com.invoiceme.domain.common.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgingReportResponse {
    private List<AgingReportData> data; // Renamed from buckets to match frontend
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgingReportData {
        private String bucket; // Renamed from range to match frontend - "0-30", "31-60", "61-90", "90+"
        private Integer count; // Renamed from invoiceCount to match frontend
        private Money amount; // Renamed from totalAmount to match frontend
    }
}

