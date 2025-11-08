package com.invoiceme.refunds;

import com.invoiceme.payments.shared.PaymentDto;
import com.invoiceme.refunds.issuerefund.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {
    
    private final IssueRefundHandler issueRefundHandler;
    private final IssueRefundMapper issueRefundMapper;
    
    @PostMapping
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<PaymentDto> issueRefund(@Valid @RequestBody IssueRefundRequest request) {
        IssueRefundCommand command = issueRefundMapper.requestToCommand(request);
        // TODO: Set createdByUserId from security context
        var refund = issueRefundHandler.handle(command);
        PaymentDto response = issueRefundMapper.toDto(refund);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

