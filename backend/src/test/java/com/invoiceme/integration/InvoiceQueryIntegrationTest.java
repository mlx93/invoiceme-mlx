package com.invoiceme.integration;

import com.invoiceme.domain.common.*;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class InvoiceQueryIntegrationTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    private Customer customer;
    
    @BeforeEach
    void setUp() {
        // Create test customer
        customer = Customer.create(
            "Query Test Company",
            Email.of("query@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
    }
    
    @Test
    void testGetInvoiceById() {
        // Create invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Service",
            1,
            Money.of(500.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(7),
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        UUID invoiceId = invoice.getId();
        
        // Retrieve by ID
        Optional<Invoice> retrieved = invoiceRepository.findById(invoiceId);
        
        // Verify
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo(invoiceId);
        assertThat(retrieved.get().getCustomerId()).isEqualTo(customer.getId());
        assertThat(retrieved.get().getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(retrieved.get().getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(535.00)); // 500 + 35 tax
    }
    
    @Test
    void testListInvoicesByStatus() {
        // Create invoices with different statuses
        Invoice draftInvoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        draftInvoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Draft Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        draftInvoice = invoiceRepository.save(draftInvoice);
        
        Invoice sentInvoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(2),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        sentInvoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Sent Item",
            1,
            Money.of(200.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        sentInvoice.markAsSent();
        sentInvoice = invoiceRepository.save(sentInvoice);
        
        // Query by status
        Page<Invoice> draftPage = invoiceRepository.findByStatus(
            InvoiceStatus.DRAFT,
            PageRequest.of(0, 10)
        );
        Page<Invoice> sentPage = invoiceRepository.findByStatus(
            InvoiceStatus.SENT,
            PageRequest.of(0, 10)
        );
        
        // Verify
        assertThat(draftPage.getContent()).isNotEmpty();
        assertThat(draftPage.getContent()).allMatch(inv -> inv.getStatus() == InvoiceStatus.DRAFT);
        
        assertThat(sentPage.getContent()).isNotEmpty();
        assertThat(sentPage.getContent()).allMatch(inv -> inv.getStatus() == InvoiceStatus.SENT);
    }
    
    @Test
    void testListInvoicesByCustomer() {
        // Create customer 2
        Customer customer2 = Customer.create(
            "Second Company",
            Email.of("second@test.com"),
            CustomerType.COMMERCIAL
        );
        customer2 = customerRepository.save(customer2);
        
        // Create invoices for both customers
        Invoice invoice1 = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        invoice1.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 1",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        invoice1 = invoiceRepository.save(invoice1);
        
        Invoice invoice2 = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(2),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        invoice2.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 2",
            1,
            Money.of(200.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        invoice2 = invoiceRepository.save(invoice2);
        
        Invoice invoice3 = Invoice.create(
            customer2.getId(),
            InvoiceNumber.generate(3),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        invoice3.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 3",
            1,
            Money.of(300.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        invoice3 = invoiceRepository.save(invoice3);
        
        // Query by customer
        UUID customer1Id = customer.getId();
        UUID customer2Id = customer2.getId();
        
        Page<Invoice> customer1Invoices = invoiceRepository.findByCustomerId(
            customer1Id,
            PageRequest.of(0, 10)
        );
        Page<Invoice> customer2Invoices = invoiceRepository.findByCustomerId(
            customer2Id,
            PageRequest.of(0, 10)
        );
        
        // Verify
        assertThat(customer1Invoices.getContent()).hasSize(2);
        assertThat(customer1Invoices.getContent())
            .allMatch(inv -> inv.getCustomerId().equals(customer1Id));
        
        assertThat(customer2Invoices.getContent()).hasSize(1);
        assertThat(customer2Invoices.getContent())
            .allMatch(inv -> inv.getCustomerId().equals(customer2Id));
    }
    
    @Test
    void testListOverdueInvoices() {
        // Create overdue invoice (due date in past)
        Invoice overdueInvoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now().minusDays(60),
            LocalDate.now().minusDays(30), // Due 30 days ago
            PaymentTerms.NET_30
        );
        overdueInvoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Overdue Item",
            1,
            Money.of(1000.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        overdueInvoice.markAsSent();
        overdueInvoice = invoiceRepository.save(overdueInvoice);
        
        // Create current invoice
        Invoice currentInvoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(2),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        currentInvoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Current Item",
            1,
            Money.of(500.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        currentInvoice.markAsSent();
        currentInvoice = invoiceRepository.save(currentInvoice);
        
        // Query overdue invoices (returns List, not Page)
        java.util.List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(LocalDate.now());
        
        // Verify
        assertThat(overdueInvoices).isNotEmpty();
        assertThat(overdueInvoices).anyMatch(inv -> 
            inv.getDueDate().isBefore(LocalDate.now()) && 
            inv.getStatus() == InvoiceStatus.SENT
        );
    }
    
    @Test
    void testListInvoicesWithBalanceDue() {
        // Create paid invoice
        Invoice paidInvoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        paidInvoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Paid Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        paidInvoice.markAsSent();
        paidInvoice.recordPayment(Money.of(100.00));
        paidInvoice = invoiceRepository.save(paidInvoice);
        
        // Create unpaid invoice
        Invoice unpaidInvoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(2),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        unpaidInvoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Unpaid Item",
            1,
            Money.of(200.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        unpaidInvoice.markAsSent();
        unpaidInvoice = invoiceRepository.save(unpaidInvoice);
        
        // Query invoices with balance due using findByFilters
        Page<Invoice> unpaidPage = invoiceRepository.findByFilters(
            java.util.List.of(InvoiceStatus.SENT, InvoiceStatus.DRAFT),
            null, // customerId
            null, // issueDateFrom
            null, // issueDateTo
            null, // dueDateFrom
            null, // dueDateTo
            null, // amountFrom
            null, // amountTo
            null, // search
            PageRequest.of(0, 10)
        );
        
        // Verify
        assertThat(unpaidPage.getContent()).isNotEmpty();
        assertThat(unpaidPage.getContent()).anyMatch(inv -> 
            inv.getBalanceDue().isPositive()
        );
    }
}

