package com.invoiceme.invoices;

import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.invoices.cancelinvoice.CancelInvoiceCommand;
import com.invoiceme.invoices.cancelinvoice.CancelInvoiceHandler;
import com.invoiceme.invoices.createinvoice.*;
import com.invoiceme.invoices.getinvoice.*;
import com.invoiceme.invoices.listinvoices.*;
import com.invoiceme.invoices.markassent.MarkAsSentCommand;
import com.invoiceme.invoices.markassent.MarkAsSentHandler;
import com.invoiceme.invoices.markassent.MarkAsSentMapper;
import com.invoiceme.invoices.shared.InvoiceDto;
import com.invoiceme.invoices.shared.LineItemDto;
import com.invoiceme.invoices.shared.PaymentSummaryDto;
import com.invoiceme.invoices.updateinvoice.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    
    // Create Invoice
    private final CreateInvoiceHandler createHandler;
    private final CreateInvoiceMapper createMapper;
    private final CreateInvoiceValidator createValidator;
    
    // Get Invoice
    private final GetInvoiceHandler getHandler;
    private final GetInvoiceMapper getMapper;
    
    // List Invoices
    private final ListInvoicesHandler listHandler;
    private final ListInvoicesMapper listMapper;
    
    // Update Invoice
    private final UpdateInvoiceHandler updateHandler;
    private final UpdateInvoiceMapper updateMapper;
    
    // Mark as Sent
    private final MarkAsSentHandler markAsSentHandler;
    private final MarkAsSentMapper markAsSentMapper;
    
    // Cancel Invoice
    private final CancelInvoiceHandler cancelHandler;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<InvoiceDto> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        createValidator.validate(request);
        CreateInvoiceCommand command = createMapper.requestToCommand(request);
        var invoice = createHandler.handle(command);
        InvoiceDto response = createMapper.toDto(invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailResponse> getInvoice(@PathVariable UUID id) {
        GetInvoiceQuery query = new GetInvoiceQuery(id);
        InvoiceDetailResult result = getHandler.handle(query);
        
        InvoiceDetailResponse response = InvoiceDetailResponse.builder()
            .id(result.getInvoice().getId())
            .invoiceNumber(result.getInvoice().getInvoiceNumber().toString())
            .customerId(result.getInvoice().getCustomerId())
            .customerName(null) // Will be populated from customer lookup if needed
            .issueDate(result.getInvoice().getIssueDate())
            .dueDate(result.getInvoice().getDueDate())
            .status(result.getInvoice().getStatus().name())
            .paymentTerms(result.getInvoice().getPaymentTerms())
            .lineItems(result.getInvoice().getLineItems().stream()
                .map(item -> {
                    LineItemDto dto = new LineItemDto();
                    dto.setId(item.getId());
                    dto.setDescription(item.getDescription());
                    dto.setQuantity(item.getQuantity());
                    dto.setUnitPrice(item.getUnitPrice());
                    dto.setDiscountType(item.getDiscountType());
                    dto.setDiscountValue(item.getDiscountValue());
                    dto.setTaxRate(item.getTaxRate());
                    dto.setLineTotal(item.calculateLineTotal());
                    dto.setSortOrder(item.getSortOrder());
                    return dto;
                })
                .collect(Collectors.toList()))
            .subtotal(result.getInvoice().getSubtotal())
            .taxAmount(result.getInvoice().getTaxAmount())
            .discountAmount(result.getInvoice().getDiscountAmount())
            .totalAmount(result.getInvoice().getTotalAmount())
            .amountPaid(result.getInvoice().getAmountPaid())
            .balanceDue(result.getInvoice().getBalanceDue())
            .notes(result.getInvoice().getNotes())
            .sentDate(result.getInvoice().getSentDate())
            .paidDate(result.getInvoice().getPaidDate())
            .payments(result.getPayments().stream()
                .map(payment -> {
                    PaymentSummaryDto dto = new PaymentSummaryDto();
                    dto.setId(payment.getId());
                    dto.setAmount(payment.getAmount());
                    dto.setPaymentMethod(payment.getPaymentMethod().name());
                    dto.setPaymentDate(payment.getPaymentDate());
                    dto.setStatus(payment.getStatus().name());
                    return dto;
                })
                .collect(Collectors.toList()))
            .pdfUrl(null) // Will be generated later
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<PagedInvoiceResponse> listInvoices(
            @RequestParam(required = false) List<InvoiceStatus> status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) java.time.LocalDate issueDateFrom,
            @RequestParam(required = false) java.time.LocalDate issueDateTo,
            @RequestParam(required = false) java.time.LocalDate dueDateFrom,
            @RequestParam(required = false) java.time.LocalDate dueDateTo,
            @RequestParam(required = false) java.math.BigDecimal amountFrom,
            @RequestParam(required = false) java.math.BigDecimal amountTo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sort) {
        
        ListInvoicesQuery query = ListInvoicesQuery.builder()
            .status(status)
            .customerId(customerId)
            .issueDateFrom(issueDateFrom)
            .issueDateTo(issueDateTo)
            .dueDateFrom(dueDateFrom)
            .dueDateTo(dueDateTo)
            .amountFrom(amountFrom)
            .amountTo(amountTo)
            .search(search)
            .page(page)
            .size(size)
            .sort(sort)
            .build();
        
        Page<com.invoiceme.domain.invoice.Invoice> invoicePage = listHandler.handle(query);
        
        PagedInvoiceResponse response = PagedInvoiceResponse.builder()
            .content(invoicePage.getContent().stream()
                .map(invoice -> {
                    InvoiceDto dto = new InvoiceDto();
                    dto.setId(invoice.getId());
                    dto.setInvoiceNumber(invoice.getInvoiceNumber().toString());
                    dto.setCustomerId(invoice.getCustomerId());
                    dto.setCustomerName(null); // Will be populated from customer lookup if needed
                    dto.setIssueDate(invoice.getIssueDate());
                    dto.setDueDate(invoice.getDueDate());
                    dto.setStatus(invoice.getStatus());
                    dto.setPaymentTerms(invoice.getPaymentTerms());
                    dto.setTotalAmount(invoice.getTotalAmount());
                    dto.setAmountPaid(invoice.getAmountPaid());
                    dto.setBalanceDue(invoice.getBalanceDue());
                    dto.setCreatedAt(invoice.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList()))
            .page(invoicePage.getNumber())
            .size(invoicePage.getSize())
            .totalElements(invoicePage.getTotalElements())
            .totalPages(invoicePage.getTotalPages())
            .first(invoicePage.isFirst())
            .last(invoicePage.isLast())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<InvoiceDto> updateInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        
        UpdateInvoiceCommand command = updateMapper.toCommand(id, request);
        var invoice = updateHandler.handle(command);
        InvoiceDto response = updateMapper.toDto(invoice);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/mark-as-sent")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<InvoiceDto> markInvoiceAsSent(@PathVariable UUID id) {
        MarkAsSentCommand command = new MarkAsSentCommand(id);
        var invoice = markAsSentHandler.handle(command);
        InvoiceDto response = markAsSentMapper.toDto(invoice);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<Void> cancelInvoice(@PathVariable UUID id) {
        CancelInvoiceCommand command = new CancelInvoiceCommand(id);
        cancelHandler.handle(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

