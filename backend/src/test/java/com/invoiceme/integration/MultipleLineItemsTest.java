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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MultipleLineItemsTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    private Customer customer;
    
    @BeforeEach
    void setUp() {
        customer = Customer.create(
            "Multi-Item Test Company",
            Email.of("multiitem@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
    }
    
    @Test
    void testInvoiceWithThreeLineItemsDifferentTaxRates() {
        // Create invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        // Add line item 1: Consulting services (taxable at 7%)
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Consulting Services",
            10, // hours
            Money.of(150.00), // per hour
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(7), // 7% tax
            0
        ));
        
        // Add line item 2: Software license (taxable at 10%)
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Software License",
            1,
            Money.of(5000.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(10), // 10% tax
            1
        ));
        
        // Add line item 3: Training materials (tax-exempt)
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Training Materials",
            5,
            Money.of(50.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO, // 0% tax
            2
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify line items
        assertThat(invoice.getLineItems()).hasSize(3);
        
        // Verify subtotals
        // Item 1: 10 × 150 = 1,500
        // Item 2: 1 × 5,000 = 5,000
        // Item 3: 5 × 50 = 250
        // Total subtotal: 6,750
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(6750.00));
        
        // Verify tax calculations
        // Item 1 tax: 1,500 × 0.07 = 105.00
        // Item 2 tax: 5,000 × 0.10 = 500.00
        // Item 3 tax: 250 × 0.00 = 0.00
        // Total tax: 605.00
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(605.00));
        
        // Verify total
        // Total: 6,750 + 605 = 7,355.00
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(7355.00));
    }
    
    @Test
    void testInvoiceWithFiveLineItemsVariousTaxRates() {
        // Create invoice with 5 line items
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        // Item 1: 8.25% tax (California rate)
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Product A",
            3,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(8.25),
            0
        ));
        
        // Item 2: 5% tax
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Product B",
            2,
            Money.of(250.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(5),
            1
        ));
        
        // Item 3: 10% tax
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Product C",
            1,
            Money.of(1000.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(10),
            2
        ));
        
        // Item 4: 0% tax (exempt)
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Product D - Tax Exempt",
            10,
            Money.of(25.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            3
        ));
        
        // Item 5: 7% tax
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Product E",
            4,
            Money.of(75.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(7),
            4
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).hasSize(5);
        
        // Subtotals: 300 + 500 + 1000 + 250 + 300 = 2,350
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(2350.00));
        
        // Taxes: 24.75 + 25.00 + 100.00 + 0.00 + 21.00 = 170.75
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(170.75));
        
        // Total: 2,350 + 170.75 = 2,520.75
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(2520.75));
    }
    
    @Test
    void testInvoiceWithHighQuantityLineItems() {
        // Test with high quantities
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        // Bulk order item 1
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Widget Model A",
            100,
            Money.of(12.50),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(8),
            0
        ));
        
        // Bulk order item 2
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Widget Model B",
            250,
            Money.of(8.75),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(8),
            1
        ));
        
        // Bulk order item 3
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Widget Model C",
            75,
            Money.of(22.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(8),
            2
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).hasSize(3);
        
        // Subtotals: 1,250 + 2,187.50 + 1,650 = 5,087.50
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(5087.50));
        
        // Tax (8% on all): 407.00
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(407.00));
        
        // Total: 5,494.50
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(5494.50));
    }
    
    @Test
    void testInvoiceWithMixedTaxableAndNonTaxableItems() {
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        // Taxable item
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Taxable Product",
            5,
            Money.of(200.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(7),
            0
        ));
        
        // Tax-exempt item (educational materials)
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Educational Books",
            10,
            Money.of(35.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            1
        ));
        
        // Another taxable item
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Office Supplies",
            20,
            Money.of(15.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(7),
            2
        ));
        
        // Tax-exempt service
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Consulting - Non-taxable",
            8,
            Money.of(125.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            3
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getLineItems()).hasSize(4);
        
        // Subtotal: 1,000 + 350 + 300 + 1,000 = 2,650
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(2650.00));
        
        // Tax: 70 + 0 + 21 + 0 = 91.00
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(91.00));
        
        // Total: 2,741.00
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(2741.00));
    }
    
    @Test
    void testLineItemOrdering() {
        // Test that line items maintain sort order
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        // Add items with explicit sort order
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "First Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0 // Sort order 0
        ));
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Second Item",
            1,
            Money.of(200.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            1 // Sort order 1
        ));
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Third Item",
            1,
            Money.of(300.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            2 // Sort order 2
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify order
        assertThat(invoice.getLineItems()).hasSize(3);
        assertThat(invoice.getLineItems().get(0).getDescription()).isEqualTo("First Item");
        assertThat(invoice.getLineItems().get(1).getDescription()).isEqualTo("Second Item");
        assertThat(invoice.getLineItems().get(2).getDescription()).isEqualTo("Third Item");
        assertThat(invoice.getLineItems().get(0).getSortOrder()).isEqualTo(0);
        assertThat(invoice.getLineItems().get(1).getSortOrder()).isEqualTo(1);
        assertThat(invoice.getLineItems().get(2).getSortOrder()).isEqualTo(2);
    }
}

