package com.invoiceme.payments;

import com.invoiceme.domain.common.PaymentMethod;
import com.invoiceme.domain.common.PaymentStatus;
import com.invoiceme.payments.getpayment.*;
import com.invoiceme.payments.listpayments.*;
import com.invoiceme.payments.recordpayment.*;
import com.invoiceme.payments.shared.PaymentDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    // Record Payment
    private final RecordPaymentHandler recordHandler;
    private final RecordPaymentMapper recordMapper;
    private final RecordPaymentValidator recordValidator;
    private final PaymentService paymentService;
    
    // Get Payment
    private final GetPaymentHandler getHandler;
    private final GetPaymentMapper getMapper;
    
    // List Payments
    private final ListPaymentsHandler listHandler;
    private final ListPaymentsMapper listMapper;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT') or (hasRole('CUSTOMER') and @paymentService.isOwnInvoice(#request.invoiceId, authentication.name))")
    public ResponseEntity<PaymentDto> recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        // Validate invoice status
        recordValidator.validate(request.getInvoiceId());
        
        // Map request to command
        RecordPaymentCommand command = recordMapper.requestToCommand(request);
        // TODO: Set createdByUserId from security context
        
        // Handle command
        var payment = recordHandler.handle(command);
        
        // Map entity to DTO
        PaymentDto response = recordMapper.toDto(payment);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDetailResponse> getPayment(@PathVariable UUID id) {
        GetPaymentQuery query = new GetPaymentQuery(id);
        var payment = getHandler.handle(query);
        
        PaymentDetailResponse response = PaymentDetailResponse.builder()
            .id(payment.getId())
            .invoiceId(payment.getInvoiceId())
            .invoiceNumber(null) // Will be populated from invoice lookup if needed
            .customerId(payment.getCustomerId())
            .customerName(null) // Will be populated from customer lookup if needed
            .amount(payment.getAmount())
            .paymentMethod(payment.getPaymentMethod().name())
            .paymentDate(payment.getPaymentDate())
            .paymentReference(payment.getPaymentReference())
            .status(payment.getStatus().name())
            .notes(payment.getNotes())
            .createdByUserId(payment.getCreatedByUserId())
            .createdByName(null) // Will be populated from user lookup if needed
            .createdAt(payment.getCreatedAt())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<PagedPaymentResponse> listPayments(
            @RequestParam(required = false) UUID invoiceId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) LocalDate paymentDateFrom,
            @RequestParam(required = false) LocalDate paymentDateTo,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sort) {
        
        ListPaymentsQuery query = ListPaymentsQuery.builder()
            .invoiceId(invoiceId)
            .customerId(customerId)
            .paymentDateFrom(paymentDateFrom)
            .paymentDateTo(paymentDateTo)
            .paymentMethod(paymentMethod)
            .status(status)
            .page(page)
            .size(size)
            .sort(sort)
            .build();
        
        Page<com.invoiceme.domain.payment.Payment> paymentPage = listHandler.handle(query);
        
        PagedPaymentResponse response = PagedPaymentResponse.builder()
            .content(paymentPage.getContent().stream()
                .map(payment -> {
                    PaymentDto dto = new PaymentDto();
                    dto.setId(payment.getId());
                    dto.setInvoiceId(payment.getInvoiceId());
                    dto.setInvoiceNumber(null); // Will be populated from invoice lookup if needed
                    dto.setCustomerId(payment.getCustomerId());
                    dto.setCustomerName(null); // Will be populated from customer lookup if needed
                    dto.setAmount(payment.getAmount());
                    dto.setPaymentMethod(payment.getPaymentMethod().name());
                    dto.setPaymentDate(payment.getPaymentDate());
                    dto.setPaymentReference(payment.getPaymentReference());
                    dto.setStatus(payment.getStatus().name());
                    dto.setCreatedAt(payment.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList()))
            .page(paymentPage.getNumber())
            .size(paymentPage.getSize())
            .totalElements(paymentPage.getTotalElements())
            .totalPages(paymentPage.getTotalPages())
            .first(paymentPage.isFirst())
            .last(paymentPage.isLast())
            .build();
        
        return ResponseEntity.ok(response);
    }
}

