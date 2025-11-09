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
public class LineItemCrudTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    private Customer customer;
    
    @BeforeEach
    void setUp() {
        customer = Customer.create(
            "LineItem Test Company",
            Email.of("lineitem@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
    }
    
    @Test
    void testAddLineItemToDraftInvoice() {
        // Create draft invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        invoice = invoiceRepository.save(invoice);
        
        assertThat(invoice.getLineItems()).isEmpty();
        
        // Add line item
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "New Service",
            2,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(7),
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).hasSize(1);
        assertThat(invoice.getLineItems().get(0).getDescription()).isEqualTo("New Service");
        assertThat(invoice.getLineItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(200.00));
    }
    
    @Test
    void testUpdateLineItemInDraftInvoice() {
        // Create draft invoice with line item
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Original Description",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(7),
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        UUID lineItemId = invoice.getLineItems().get(0).getId();
        
        // Update line item by removing old and adding new (updateLineItem doesn't exist)
        invoice.removeLineItem(lineItemId);
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Updated Description",
            3,
            Money.of(150.00),
            DiscountType.PERCENTAGE,
            Money.of(10),
            java.math.BigDecimal.valueOf(8),
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).hasSize(1);
        assertThat(invoice.getLineItems().get(0).getDescription()).isEqualTo("Updated Description");
        assertThat(invoice.getLineItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(invoice.getLineItems().get(0).getUnitPrice().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(150.00));
        assertThat(invoice.getLineItems().get(0).getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
        
        // Subtotal: 3 × 150 = 450
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(450.00));
    }
    
    @Test
    void testRemoveLineItemFromDraftInvoice() {
        // Create draft invoice with multiple line items
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
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
            1
        ));
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 3",
            1,
            Money.of(300.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            2
        ));
        
        invoice = invoiceRepository.save(invoice);
        assertThat(invoice.getLineItems()).hasSize(3);
        
        // Remove middle item
        UUID itemToRemove = invoice.getLineItems().get(1).getId();
        invoice.removeLineItem(itemToRemove);
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).hasSize(2);
        assertThat(invoice.getLineItems().get(0).getDescription()).isEqualTo("Item 1");
        assertThat(invoice.getLineItems().get(1).getDescription()).isEqualTo("Item 3");
        
        // Subtotal: 100 + 300 = 400
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(400.00));
    }
    
    @Test
    void testRemoveAllLineItemsFromDraftInvoice() {
        // Create invoice with line items
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
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
            1
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Remove all items
        UUID item1Id = invoice.getLineItems().get(0).getId();
        UUID item2Id = invoice.getLineItems().get(1).getId();
        
        invoice.removeLineItem(item1Id);
        invoice.removeLineItem(item2Id);
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).isEmpty();
        assertThat(invoice.getSubtotal().isZero()).isTrue();
        assertThat(invoice.getTotalAmount().isZero()).isTrue();
    }
    
    @Test
    void testCannotAddLineItemToSentInvoice() {
        // Create and send invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Initial Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        
        // Attempt to add line item
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
    void testCannotRemoveLineItemFromSentInvoice() {
        // Create invoice with line items
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
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
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        UUID lineItemId = invoice.getLineItems().get(0).getId();
        
        // Attempt to remove line item
        Invoice finalInvoice = invoice;
        assertThatThrownBy(() -> 
            finalInvoice.removeLineItem(lineItemId)
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Cannot remove line items");
    }
    
    @Test
    void testCannotAddLineItemToPaidInvoice() {
        // Create, send, and pay invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Initial Item",
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
        
        // Attempt to add line item
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
    void testAddMultipleLineItemsSequentially() {
        // Create draft invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        invoice = invoiceRepository.save(invoice);
        
        // Add first line item
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 1",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        invoice = invoiceRepository.save(invoice);
        assertThat(invoice.getLineItems()).hasSize(1);
        
        // Add second line item
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 2",
            1,
            Money.of(200.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            1
        ));
        invoice = invoiceRepository.save(invoice);
        assertThat(invoice.getLineItems()).hasSize(2);
        
        // Add third line item
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Item 3",
            1,
            Money.of(300.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            2
        ));
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).hasSize(3);
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(600.00));
    }
    
    @Test
    void testUpdateThenRemoveLineItem() {
        // Create invoice with line items
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
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
        
        // Update line item by removing old and adding new (updateLineItem doesn't exist)
        invoice.removeLineItem(lineItemId);
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Updated Item",
            2,
            Money.of(150.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        invoice = invoiceRepository.save(invoice);
        
        assertThat(invoice.getLineItems().get(0).getDescription()).isEqualTo("Updated Item");
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(300.00));
        
        // Remove line item (get the new ID)
        UUID updatedLineItemId = invoice.getLineItems().get(0).getId();
        invoice.removeLineItem(updatedLineItemId);
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).isEmpty();
        assertThat(invoice.getSubtotal().isZero()).isTrue();
    }
}

