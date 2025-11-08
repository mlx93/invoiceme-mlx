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
    private List<AgingBucket> buckets;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgingBucket {
        private String range; // "0-30", "31-60", "61-90", "90+"
        private Integer invoiceCount;
        private Money totalAmount;
    }
}

