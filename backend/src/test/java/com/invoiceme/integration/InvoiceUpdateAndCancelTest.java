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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class InvoiceUpdateAndCancelTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    private Customer customer;
    private static long invoiceNumberCounter = System.nanoTime() + 20000;
    
    private InvoiceNumber generateUniqueInvoiceNumber() {
        // Use nanoTime modulo to get a unique sequence number per test run
        return InvoiceNumber.generate((int)((invoiceNumberCounter++ % 9999) + 1));
    }
    
    @BeforeEach
    void setUp() {
        customer = Customer.create(
            "Update Test Company",
            Email.of("update@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
    }
    
    @Test
    void testUpdateDraftInvoice() {
        // Create draft invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Original Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        UUID lineItemId = invoice.getLineItems().get(0).getId();
        
        // Update line item by adding new first, then removing old (must maintain at least one line item)
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Updated Item",
            2,
            Money.of(150.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        invoice.removeLineItem(lineItemId);
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).hasSize(1);
        assertThat(invoice.getLineItems().get(0).getDescription()).isEqualTo("Updated Item");
        assertThat(invoice.getLineItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(invoice.getLineItems().get(0).getUnitPrice().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(150.00));
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(300.00)); // 2 × 150
    }
    
    @Test
    void testUpdateInvoiceNotes() {
        // Create invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Update notes
        String notes = "Please pay within 30 days. Thank you for your business!";
        invoice.updateNotes(notes);
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getNotes()).isEqualTo(notes);
    }
    
    @Test
    void testUpdateInvoiceDueDate() {
        // Create invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        LocalDate originalDueDate = invoice.getDueDate();
        
        // Update due date using updateDates
        LocalDate newDueDate = LocalDate.now().plusDays(45);
        invoice.updateDates(invoice.getIssueDate(), newDueDate);
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getDueDate()).isEqualTo(newDueDate);
        assertThat(invoice.getDueDate()).isNotEqualTo(originalDueDate);
    }
    
    @Test
    void testCannotUpdateSentInvoiceLineItems() {
        // Create and send invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        
        // Attempt to add line item to sent invoice
        Invoice finalInvoice = invoice;
        assertThatThrownBy(() -> 
            finalInvoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
                "New Item",
                1,
                Money.of(50.00),
                DiscountType.NONE,
                Money.zero(),
                java.math.BigDecimal.ZERO,
                0
            ))
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Cannot add line items");
    }
    
    @Test
    void testCancelDraftInvoice() {
        // Create draft invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Cancel invoice
        invoice.cancel();
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
    }
    
    @Test
    void testCancelSentInvoice() {
        // Create and send invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        
        // Cancel invoice
        invoice.cancel();
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
    }
    
    @Test
    void testCannotCancelPaidInvoice() {
        // Create, send, and pay invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice.recordPayment(Money.of(100.00));
        invoice = invoiceRepository.save(invoice);
        
        // Attempt to cancel paid invoice
        Invoice finalInvoice = invoice;
        assertThatThrownBy(() -> 
            finalInvoice.cancel()
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Cannot cancel");
    }
    
    @Test
    void testAddMultipleLineItemsToDraftInvoice() {
        // Create draft invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        // Add first line item
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 1",
            2,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Add second line item
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 2",
            1,
            Money.of(250.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Add third line item
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 3",
            3,
            Money.of(75.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).hasSize(3);
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(675.00)); // 200 + 250 + 225
    }
    
    @Test
    void testRemoveLineItemFromDraftInvoice() {
        // Create draft invoice with multiple line items
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 1",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 2",
            1,
            Money.of(200.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        UUID lineItemToRemove = invoice.getLineItems().get(0).getId();
        
        // Remove first line item
        invoice.removeLineItem(lineItemToRemove);
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).hasSize(1);
        assertThat(invoice.getLineItems().get(0).getDescription()).isEqualTo("Item 2");
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(200.00));
    }
}

