package com.invoiceme.integration;

/**
 * NOTE: Refund tests are not included here because refunds are handled at the application layer
 * via IssueRefundHandler, not at the domain layer.
 * 
 * Refunds:
 * - Create customer credits (via Customer.applyCredit())
 * - Create refund Payment records (via Payment.createRefund())
 * - Do NOT change invoice status (invoice remains PAID)
 * - Are tested at the application/API layer, not domain layer
 * 
 * To test refunds, create application-layer integration tests that use IssueRefundHandler.
 */
