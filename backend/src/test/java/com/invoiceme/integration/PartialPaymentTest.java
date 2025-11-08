package com.invoiceme.integration;

import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.common.PaymentMethod;
import com.invoiceme.domain.common.PaymentTerms;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PartialPaymentTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    private Customer customer;
    private Invoice invoice;
    
    @BeforeEach
    void setUp() {
        customer = Customer.create(
            "Test Company",
            "John Doe",
            com.invoiceme.domain.common.Email.of("test@example.com"),
            "555-1234",
            com.invoiceme.domain.common.CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
        
        invoice = Invoice.create(
            customer.getId(),
            com.invoiceme.domain.common.InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(1000.00),
            com.invoiceme.domain.common.DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
    }
    
    @Test
    void testPartialPayment() {
        // Record partial payment
        Money partialPayment = Money.of(500.00);
        Payment payment = Payment.record(
            invoice,
            customer,
            partialPayment,
            PaymentMethod.ACH,
            LocalDate.now(),
            UUID.randomUUID()
        );
        
        payment = paymentRepository.save(payment);
        invoice = invoiceRepository.save(invoice);
        
        // Verify partial payment
        assertThat(payment.getAmount()).isEqualTo(partialPayment);
        assertThat(invoice.getAmountPaid().getAmount()).isEqualByComparingTo(java.math.BigDecimal.valueOf(500.00));
        assertThat(invoice.getBalanceDue().getAmount()).isEqualByComparingTo(java.math.BigDecimal.valueOf(500.00));
        assertThat(invoice.getStatus()).isEqualTo(com.invoiceme.domain.common.InvoiceStatus.SENT); // Still SENT, not PAID
        
        // Record second partial payment
        Money secondPayment = Money.of(500.00);
        Payment payment2 = Payment.record(
            invoice,
            customer,
            secondPayment,
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            UUID.randomUUID()
        );
        
        payment2 = paymentRepository.save(payment2);
        invoice = invoiceRepository.save(invoice);
        
        // Verify invoice fully paid
        assertThat(invoice.getAmountPaid().getAmount()).isEqualByComparingTo(java.math.BigDecimal.valueOf(1000.00));
        assertThat(invoice.getBalanceDue().getAmount()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
        assertThat(invoice.getStatus()).isEqualTo(com.invoiceme.domain.common.InvoiceStatus.PAID);
    }
}

