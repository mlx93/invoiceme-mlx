package com.invoiceme.dashboard.getinvoicestatus;

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
public class InvoiceStatusResponse {
    private List<InvoiceStatusData> data; // Renamed from breakdown to match frontend
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceStatusData {
        private String status;
        private Integer count;
        private Money amount; // Renamed from totalAmount to match frontend
    }
}

