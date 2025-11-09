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
public class DiscountCalculationTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    private Customer customer;
    private static long invoiceNumberCounter = System.nanoTime() + 50000;
    
    private InvoiceNumber generateUniqueInvoiceNumber() {
        // Use nanoTime modulo to get a unique sequence number per test run
        return InvoiceNumber.generate((int)((invoiceNumberCounter++ % 9999) + 1));
    }
    
    @BeforeEach
    void setUp() {
        customer = Customer.create(
            "Discount Test Company",
            Email.of("discount@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
    }
    
    @Test
    void testPercentageDiscount() {
        // Create invoice with percentage discount
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        // Add line item with 15% discount
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Consulting Services",
            10, // hours
            Money.of(100.00), // per hour
            DiscountType.PERCENTAGE,
            Money.of(15), // 15% discount
            java.math.BigDecimal.valueOf(7), // 7% tax
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify calculations
        // Base: 10 × 100 = 1,000
        // Discount: 1,000 × 15% = 150
        // Taxable: 1,000 - 150 = 850
        // Tax: 850 × 7% = 59.50
        // Total: 850 + 59.50 = 909.50
        
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(1000.00));
        assertThat(invoice.getDiscountAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(150.00));
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(59.50));
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(909.50));
    }
    
    @Test
    void testFixedAmountDiscount() {
        // Create invoice with fixed discount
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        // Add line item with $250 fixed discount
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Software License",
            1,
            Money.of(2000.00),
            DiscountType.FIXED,
            Money.of(250), // $250 off
            java.math.BigDecimal.valueOf(10), // 10% tax
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify calculations
        // Base: 2,000
        // Discount: 250 (fixed)
        // Taxable: 2,000 - 250 = 1,750
        // Tax: 1,750 × 10% = 175
        // Total: 1,750 + 175 = 1,925
        
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(2000.00));
        assertThat(invoice.getDiscountAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(250.00));
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(175.00));
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(1925.00));
    }
    
    @Test
    void testMultipleLineItemsWithDifferentDiscounts() {
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        // Item 1: 20% percentage discount
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Product A",
            5,
            Money.of(100.00),
            DiscountType.PERCENTAGE,
            Money.of(20), // 20% off
            java.math.BigDecimal.valueOf(8),
            0
        ));
        
        // Item 2: $75 fixed discount
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Product B",
            2,
            Money.of(500.00),
            DiscountType.FIXED,
            Money.of(75), // $75 off
            java.math.BigDecimal.valueOf(8),
            1
        ));
        
        // Item 3: No discount
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Product C",
            1,
            Money.of(300.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(8),
            2
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify calculations
        // Item 1: Base 500, Discount 100, Taxable 400, Tax 32, Total 432
        // Item 2: Base 1000, Discount 75, Taxable 925, Tax 74, Total 999
        // Item 3: Base 300, Discount 0, Taxable 300, Tax 24, Total 324
        // Totals: Subtotal 1800, Discount 175, Tax 130, Total 1755
        
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(1800.00));
        assertThat(invoice.getDiscountAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(175.00));
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(130.00));
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(1755.00));
    }
    
    @Test
    void testHighPercentageDiscount() {
        // Test 50% discount
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Clearance Item",
            3,
            Money.of(200.00),
            DiscountType.PERCENTAGE,
            Money.of(50), // 50% off
            java.math.BigDecimal.valueOf(7),
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Base: 600
        // Discount: 300 (50%)
        // Taxable: 300
        // Tax: 21
        // Total: 321
        
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(600.00));
        assertThat(invoice.getDiscountAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(300.00));
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(21.00));
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(321.00));
    }
    
    @Test
    void testSmallPercentageDiscount() {
        // Test 5% discount
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Standard Item",
            1,
            Money.of(1000.00),
            DiscountType.PERCENTAGE,
            Money.of(5), // 5% off
            java.math.BigDecimal.valueOf(8),
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Base: 1,000
        // Discount: 50 (5%)
        // Taxable: 950
        // Tax: 76
        // Total: 1,026
        
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(1000.00));
        assertThat(invoice.getDiscountAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(50.00));
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(76.00));
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(1026.00));
    }
    
    @Test
    void testFixedDiscountCannotExceedItemPrice() {
        // Test that fixed discount is capped at item price
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Low Price Item",
            1,
            Money.of(50.00),
            DiscountType.FIXED,
            Money.of(100), // Discount exceeds price
            java.math.BigDecimal.valueOf(7),
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Discount should be capped at base amount (50)
        // Base: 50
        // Discount: 50 (capped, not 100)
        // Taxable: 0
        // Tax: 0
        // Total: 0
        
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(50.00));
        assertThat(invoice.getDiscountAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(50.00));
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.ZERO);
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.ZERO);
    }
    
    @Test
    void testNoDiscountApplied() {
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Regular Price Item",
            2,
            Money.of(150.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(8),
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Base: 300
        // Discount: 0
        // Taxable: 300
        // Tax: 24
        // Total: 324
        
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(300.00));
        assertThat(invoice.getDiscountAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.ZERO);
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(24.00));
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(324.00));
    }
    
    @Test
    void testDiscountAppliedBeforeTax() {
        // Verify discount is applied before tax calculation
        Invoice invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Discounted Item",
            1,
            Money.of(1000.00),
            DiscountType.PERCENTAGE,
            Money.of(25), // 25% discount
            java.math.BigDecimal.valueOf(10), // 10% tax
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Base: 1,000
        // Discount: 250 (25%)
        // Taxable: 750 (discount applied first)
        // Tax: 75 (10% of 750, not 10% of 1000)
        // Total: 825
        
        assertThat(invoice.getSubtotal().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(1000.00));
        assertThat(invoice.getDiscountAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(250.00));
        assertThat(invoice.getTaxAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(75.00)); // Tax on 750, not 1000
        assertThat(invoice.getTotalAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(825.00));
    }
}

