package com.invoiceme.infrastructure.email;

import com.invoiceme.domain.common.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AwsSesEmailService implements EmailService {
    
    private final SesClient sesClient;
    
    @Value("${aws.ses.from-email}")
    private String fromEmail;
    
    @Override
    public void sendInvoiceEmail(String customerEmail, String invoiceNumber, UUID invoiceId) {
        try {
            String subject = "Invoice " + invoiceNumber + " from InvoiceMe";
            String body = String.format(
                "Dear Customer,\n\n" +
                "Your invoice %s has been sent. Please find the invoice PDF attached.\n\n" +
                "Payment Link: https://invoiceme.com/pay/%s\n\n" +
                "Thank you for your business!",
                invoiceNumber, invoiceId
            );
            
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                    .toAddresses(customerEmail)
                    .build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder()
                        .text(Content.builder().data(body).build())
                        .build())
                    .build())
                .build();
            
            sesClient.sendEmail(request);
            log.info("Invoice email sent to {} for invoice {}", customerEmail, invoiceNumber);
        } catch (Exception e) {
            log.error("Failed to send invoice email to {} for invoice {}", customerEmail, invoiceNumber, e);
            // Don't throw - email failures shouldn't break the transaction
        }
    }
    
    @Override
    public void sendPaymentConfirmation(String customerEmail, String invoiceNumber, Money amount) {
        try {
            String subject = "Payment Received - Invoice " + invoiceNumber;
            String body = String.format(
                "Dear Customer,\n\n" +
                "We have received your payment of %s for invoice %s.\n\n" +
                "Thank you!",
                amount, invoiceNumber
            );
            
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                    .toAddresses(customerEmail)
                    .build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder()
                        .text(Content.builder().data(body).build())
                        .build())
                    .build())
                .build();
            
            sesClient.sendEmail(request);
            log.info("Payment confirmation email sent to {} for invoice {}", customerEmail, invoiceNumber);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to {} for invoice {}", customerEmail, invoiceNumber, e);
        }
    }
    
    @Override
    public void sendPaymentCompletion(String customerEmail, String invoiceNumber, Money totalAmount) {
        try {
            String subject = "Invoice Paid in Full - " + invoiceNumber;
            String body = String.format(
                "Dear Customer,\n\n" +
                "Your invoice %s has been paid in full (Total: %s).\n\n" +
                "Thank you for your payment!",
                invoiceNumber, totalAmount
            );
            
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                    .toAddresses(customerEmail)
                    .build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder()
                        .text(Content.builder().data(body).build())
                        .build())
                    .build())
                .build();
            
            sesClient.sendEmail(request);
            log.info("Payment completion email sent to {} for invoice {}", customerEmail, invoiceNumber);
        } catch (Exception e) {
            log.error("Failed to send payment completion email to {} for invoice {}", customerEmail, invoiceNumber, e);
        }
    }
    
    @Override
    public void sendOverdueReminder(String customerEmail, String invoiceNumber, Money lateFeeAmount, Money newBalance) {
        try {
            String subject = "Payment Reminder - Invoice " + invoiceNumber + " Overdue";
            String body = String.format(
                "Dear Customer,\n\n" +
                "Your invoice %s is overdue. A late fee of %s has been applied.\n\n" +
                "New Balance: %s\n\n" +
                "Please make payment as soon as possible.",
                invoiceNumber, lateFeeAmount, newBalance
            );
            
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                    .toAddresses(customerEmail)
                    .build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder()
                        .text(Content.builder().data(body).build())
                        .build())
                    .build())
                .build();
            
            sesClient.sendEmail(request);
            log.info("Overdue reminder email sent to {} for invoice {}", customerEmail, invoiceNumber);
        } catch (Exception e) {
            log.error("Failed to send overdue reminder email to {} for invoice {}", customerEmail, invoiceNumber, e);
        }
    }
}

