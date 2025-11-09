package com.invoiceme.integration;

import com.invoiceme.domain.common.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PaymentQueryIntegrationTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    private Customer customer;
    private Invoice invoice;
    private static long invoiceNumberCounter = System.nanoTime() + 10000;
    
    private InvoiceNumber generateUniqueInvoiceNumber() {
        // Use nanoTime modulo to get a unique sequence number per test run
        return InvoiceNumber.generate((int)((invoiceNumberCounter++ % 9999) + 1));
    }
    
    @BeforeEach
    void setUp() {
        // Create test customer
        customer = Customer.create(
            "Payment Test Company",
            Email.of("payment@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
        
        // Create test invoice
        invoice = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Service",
            1,
            Money.of(1000.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
    }
    
    @Test
    void testGetPaymentById() {
        // Record payment
        Payment payment = Payment.record(
            invoice,
            customer,
            Money.of(500.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        payment = paymentRepository.save(payment);
        UUID paymentId = payment.getId();
        
        // Retrieve by ID
        Optional<Payment> retrieved = paymentRepository.findById(paymentId);
        
        // Verify
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo(paymentId);
        assertThat(retrieved.get().getInvoiceId()).isEqualTo(invoice.getId());
        assertThat(retrieved.get().getCustomerId()).isEqualTo(customer.getId());
        assertThat(retrieved.get().getAmount().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(500.00));
        assertThat(retrieved.get().getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(retrieved.get().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }
    
    @Test
    void testListPaymentsForInvoice() {
        // Record multiple payments for same invoice
        Payment payment1 = Payment.record(
            invoice,
            customer,
            Money.of(300.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        payment1 = paymentRepository.save(payment1);
        // Payment.record() already calls invoice.recordPayment(), just save the invoice
        invoice = invoiceRepository.save(invoice);
        
        // Reload invoice to get updated status
        invoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
        
        Payment payment2 = Payment.record(
            invoice,
            customer,
            Money.of(400.00),
            PaymentMethod.ACH,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        payment2 = paymentRepository.save(payment2);
        // Payment.record() already calls invoice.recordPayment(), just save the invoice
        invoice = invoiceRepository.save(invoice);
        
        // Reload invoice to get updated status
        invoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
        
        // Record third payment (invoice total is $1000, so $300 + $400 + $300 = $1000, all should be recordable)
        Payment payment3 = Payment.record(
            invoice,
            customer,
            Money.of(300.00),
            PaymentMethod.ACH,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        payment3 = paymentRepository.save(payment3);
        // Payment.record() already calls invoice.recordPayment(), just save the invoice
        invoice = invoiceRepository.save(invoice);
        
        // Query payments for invoice
        Page<Payment> paymentPage = paymentRepository.findByInvoiceId(invoice.getId(), PageRequest.of(0, 10));
        List<Payment> payments = paymentPage.getContent();
        
        // Verify
        assertThat(payments).hasSize(3);
        assertThat(payments).extracting("amount")
            .extracting("amount")
            .usingElementComparator((a, b) -> ((java.math.BigDecimal) a).compareTo((java.math.BigDecimal) b))
            .containsExactlyInAnyOrder(
                java.math.BigDecimal.valueOf(300.00),
                java.math.BigDecimal.valueOf(400.00),
                java.math.BigDecimal.valueOf(300.00)
            );
        assertThat(payments).allMatch(p -> p.getInvoiceId().equals(invoice.getId()));
    }
    
    @Test
    void testListPaymentsForCustomer() {
        // Create second invoice for same customer
        Invoice invoice2 = Invoice.create(
            customer.getId(),
            generateUniqueInvoiceNumber(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        invoice2.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Second Service",
            1,
            Money.of(750.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        invoice2.markAsSent();
        invoice2 = invoiceRepository.save(invoice2);
        
        // Record payments for both invoices
        Payment payment1 = Payment.record(
            invoice,
            customer,
            Money.of(500.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        payment1 = paymentRepository.save(payment1);
        
        Payment payment2 = Payment.record(
            invoice2,
            customer,
            Money.of(750.00),
            PaymentMethod.ACH,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        payment2 = paymentRepository.save(payment2);
        
        // Query payments for customer
        Page<Payment> customerPaymentPage = paymentRepository.findByCustomerId(customer.getId(), PageRequest.of(0, 10));
        List<Payment> customerPayments = customerPaymentPage.getContent();
        
        // Verify
        assertThat(customerPayments).hasSize(2);
        assertThat(customerPayments).allMatch(p -> p.getCustomerId().equals(customer.getId()));
        assertThat(customerPayments).extracting("amount")
            .extracting("amount")
            .usingElementComparator((a, b) -> ((java.math.BigDecimal) a).compareTo((java.math.BigDecimal) b))
            .containsExactlyInAnyOrder(
                java.math.BigDecimal.valueOf(500.00),
                java.math.BigDecimal.valueOf(750.00)
            );
    }
    
    @Test
    void testListPaymentsByPaymentMethod() {
        // Record payments with different methods
        Payment creditCardPayment = Payment.record(
            invoice,
            customer,
            Money.of(200.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        creditCardPayment = paymentRepository.save(creditCardPayment);
        
        Payment achPayment = Payment.record(
            invoice,
            customer,
            Money.of(300.00),
            PaymentMethod.ACH,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        achPayment = paymentRepository.save(achPayment);
        
        Payment achPayment2 = Payment.record(
            invoice,
            customer,
            Money.of(150.00),
            PaymentMethod.ACH,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        achPayment2 = paymentRepository.save(achPayment2);
        
        // Query by payment method
        Page<Payment> creditCardPage = paymentRepository.findByPaymentMethod(
            PaymentMethod.CREDIT_CARD,
            PageRequest.of(0, 10)
        );
        Page<Payment> achPage = paymentRepository.findByPaymentMethod(
            PaymentMethod.ACH,
            PageRequest.of(0, 10)
        );
        
        // Verify
        assertThat(creditCardPage.getContent()).isNotEmpty();
        assertThat(creditCardPage.getContent())
            .allMatch(p -> p.getPaymentMethod() == PaymentMethod.CREDIT_CARD);
        
        assertThat(achPage.getContent()).isNotEmpty();
        assertThat(achPage.getContent())
            .allMatch(p -> p.getPaymentMethod() == PaymentMethod.ACH);
    }
    
    @Test
    void testListPaymentsByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        // Record payments on different dates
        Payment oldPayment = Payment.record(
            invoice,
            customer,
            Money.of(100.00),
            PaymentMethod.CREDIT_CARD,
            startDate.minusDays(10), // Outside range
            null // createdByUserId - can be null for tests
        );
        oldPayment = paymentRepository.save(oldPayment);
        
        Payment recentPayment = Payment.record(
            invoice,
            customer,
            Money.of(200.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(), // Within range
            null // createdByUserId - can be null for tests
        );
        recentPayment = paymentRepository.save(recentPayment);
        
        // Query by date range using findByFilters
        Page<Payment> paymentsInRange = paymentRepository.findByFilters(
            null, // invoiceId
            null, // customerId
            startDate, // paymentDateFrom
            endDate, // paymentDateTo
            null, // paymentMethod
            null, // status
            PageRequest.of(0, 10)
        );
        
        // Verify
        assertThat(paymentsInRange.getContent()).isNotEmpty();
        assertThat(paymentsInRange.getContent())
            .allMatch(p -> 
                !p.getPaymentDate().isBefore(startDate) && 
                !p.getPaymentDate().isAfter(endDate)
            );
    }
    
    @Test
    void testGetTotalPaymentsForInvoice() {
        // Record multiple payments
        Payment payment1 = Payment.record(
            invoice,
            customer,
            Money.of(250.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        paymentRepository.save(payment1);
        
        Payment payment2 = Payment.record(
            invoice,
            customer,
            Money.of(350.00),
            PaymentMethod.ACH,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        paymentRepository.save(payment2);
        
        Payment payment3 = Payment.record(
            invoice,
            customer,
            Money.of(400.00),
            PaymentMethod.ACH,
            LocalDate.now(),
            null // createdByUserId - can be null for tests
        );
        paymentRepository.save(payment3);
        
        // Get all payments and sum
        Page<Payment> paymentPage = paymentRepository.findByInvoiceId(invoice.getId(), PageRequest.of(0, 10));
        List<Payment> allPayments = paymentPage.getContent();
        Money totalPaid = allPayments.stream()
            .map(Payment::getAmount)
            .reduce(Money.zero(), Money::add);
        
        // Verify
        assertThat(totalPaid.getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(1000.00));
    }
}

