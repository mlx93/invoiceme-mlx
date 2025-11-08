package com.invoiceme.refunds.issuerefund;

import com.invoiceme.domain.common.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueRefundCommand {
    private UUID invoiceId;
    private Money amount;
    private String reason;
    private Boolean applyAsCredit;
    private UUID createdByUserId;
}

