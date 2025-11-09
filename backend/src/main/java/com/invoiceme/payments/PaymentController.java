package com.invoiceme.payments;

import com.invoiceme.domain.common.PaymentMethod;
import com.invoiceme.domain.common.PaymentStatus;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
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
import java.util.List;
import java.util.Map;
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
    
    // Repositories for batch loading invoice numbers and customer names
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    
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
        
        // Populate invoice number and customer name
        Invoice invoice = invoiceRepository.findById(payment.getInvoiceId())
            .orElse(null);
        Customer customer = customerRepository.findById(payment.getCustomerId())
            .orElse(null);
        
        if (invoice != null) {
            response.setInvoiceNumber(invoice.getInvoiceNumber().toString());
        }
        if (customer != null) {
            response.setCustomerName(customer.getCompanyName());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDetailResponse> getPayment(@PathVariable UUID id) {
        GetPaymentQuery query = new GetPaymentQuery(id);
        var payment = getHandler.handle(query);
        
        // Load invoice and customer to populate invoice number and customer name
        Invoice invoice = invoiceRepository.findById(payment.getInvoiceId())
            .orElse(null);
        Customer customer = customerRepository.findById(payment.getCustomerId())
            .orElse(null);
        
        PaymentDetailResponse response = PaymentDetailResponse.builder()
            .id(payment.getId())
            .invoiceId(payment.getInvoiceId())
            .invoiceNumber(invoice != null ? invoice.getInvoiceNumber().toString() : "Unknown")
            .customerId(payment.getCustomerId())
            .customerName(customer != null ? customer.getCompanyName() : "Unknown Customer")
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
        
        // Batch load invoice numbers and customer names for performance
        List<UUID> invoiceIds = paymentPage.getContent().stream()
            .map(com.invoiceme.domain.payment.Payment::getInvoiceId)
            .distinct()
            .collect(Collectors.toList());
        
        List<UUID> customerIds = paymentPage.getContent().stream()
            .map(com.invoiceme.domain.payment.Payment::getCustomerId)
            .distinct()
            .collect(Collectors.toList());
        
        Map<UUID, String> invoiceNumberMap = invoiceRepository.findAllById(invoiceIds).stream()
            .collect(Collectors.toMap(Invoice::getId, invoice -> invoice.getInvoiceNumber().toString()));
        
        Map<UUID, String> customerNameMap = customerRepository.findAllById(customerIds).stream()
            .collect(Collectors.toMap(Customer::getId, Customer::getCompanyName));
        
        PagedPaymentResponse response = PagedPaymentResponse.builder()
            .content(paymentPage.getContent().stream()
                .map(payment -> {
                    PaymentDto dto = new PaymentDto();
                    dto.setId(payment.getId());
                    dto.setInvoiceId(payment.getInvoiceId());
                    dto.setInvoiceNumber(invoiceNumberMap.getOrDefault(payment.getInvoiceId(), "Unknown"));
                    dto.setCustomerId(payment.getCustomerId());
                    dto.setCustomerName(customerNameMap.getOrDefault(payment.getCustomerId(), "Unknown Customer"));
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

