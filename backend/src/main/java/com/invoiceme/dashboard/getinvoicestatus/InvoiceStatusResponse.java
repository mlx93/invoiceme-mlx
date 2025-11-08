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
    private List<StatusBreakdown> breakdown;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusBreakdown {
        private String status;
        private Integer count;
        private Money totalAmount;
    }
}

