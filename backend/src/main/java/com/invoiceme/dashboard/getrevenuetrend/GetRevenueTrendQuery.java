package com.invoiceme.dashboard.getrevenuetrend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetRevenueTrendQuery {
    private LocalDate startDate;
    private LocalDate endDate;
    private String period; // MONTHLY, WEEKLY, DAILY
}

