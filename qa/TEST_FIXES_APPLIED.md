# Test Fixes Applied

**Date:** November 9, 2025  
**Status:** ✅ Invoice numbers fixed, ✅ Line item update logic fixed

---

## ✅ Fixes Applied

### 1. Unique Invoice Numbers ✅

**Problem:** Multiple tests used `InvoiceNumber.generate(1)`, creating duplicate invoice numbers (`INV-2025-0001`) and violating unique constraint.

**Solution:** Added a static counter to each test class and a helper method `generateUniqueInvoiceNumber()` that increments the counter for each invoice created.

**Files Fixed:**
- ✅ `InvoiceQueryIntegrationTest.java` - Counter starts at 1
- ✅ `PaymentQueryIntegrationTest.java` - Counter starts at 1000
- ✅ `InvoiceUpdateAndCancelTest.java` - Counter starts at 2000
- ✅ `InvoiceStateTransitionTest.java` - Counter starts at 3000
- ✅ `LineItemCrudTest.java` - Counter starts at 4000
- ✅ `DiscountCalculationTest.java` - Counter starts at 5000
- ✅ `MultipleLineItemsTest.java` - Counter starts at 6000

**Impact:** All 10 tests with duplicate invoice number errors should now pass.

---

### 2. Line Item Update Logic ✅

**Problem:** Tests were removing line items before adding new ones, violating the domain rule "Invoice must have at least one line item".

**Solution:** Changed all line item updates to add the new item first, then remove the old item.

**Files Fixed:**
- ✅ `InvoiceUpdateAndCancelTest.testUpdateDraftInvoice`
- ✅ `LineItemCrudTest.testUpdateLineItemInDraftInvoice`
- ✅ `LineItemCrudTest.testUpdateThenRemoveLineItem`

**Additional Fixes:**
- ✅ `LineItemCrudTest.testRemoveAllLineItemsFromDraftInvoice` - Changed to verify that removing the last item throws an exception
- ✅ `LineItemCrudTest.testUpdateThenRemoveLineItem` - Changed to verify that removing the last item throws an exception

**Impact:** All 5 tests with "Invoice must have at least one line item" errors should now pass.

---

### 3. Payment Recording Logic ✅

**Problem:** Tests were recording multiple payments without reloading the invoice, causing attempts to record payments on already PAID invoices.

**Solution:** Added invoice reload after each payment to get the updated status.

**Files Fixed:**
- ✅ `PaymentQueryIntegrationTest.testListPaymentsForInvoice` - Added reloads after each payment

**Impact:** Should fix the "Can only record payment for SENT or OVERDUE invoices. Current status: PAID" error.

---

## ⚠️ Domain Rule Discrepancy Found

### SENT Invoices Can Have Line Items Added (But Shouldn't)

**Issue:** The domain code in `Invoice.addLineItem()` only prevents adding line items to PAID and CANCELLED invoices. SENT invoices are NOT prevented:

```java
public void addLineItem(LineItem lineItem) {
    if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED) {
        throw new IllegalStateException("Cannot add line items to " + status + " invoice");
    }
    // ... allows SENT invoices
}
```

**User Requirement:** "after a user sends an invoice, that can only cancel it, not edit it, so adding line items is irrelevant."

**Tests Affected:**
- `InvoiceUpdateAndCancelTest.testCannotUpdateSentInvoiceLineItems` - Expects exception when adding to SENT invoice
- `LineItemCrudTest.testCannotAddLineItemToSentInvoice` - Expects exception when adding to SENT invoice
- `LineItemCrudTest.testCannotRemoveLineItemFromSentInvoice` - Expects exception when removing from SENT invoice

**Current Behavior:** Domain allows adding/removing line items to/from SENT invoices.

**Required Change:** Domain code needs to be updated to prevent editing SENT invoices:
```java
public void addLineItem(LineItem lineItem) {
    if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED || status == InvoiceStatus.SENT) {
        throw new IllegalStateException("Cannot add line items to " + status + " invoice");
    }
    // ...
}

public void removeLineItem(UUID lineItemId) {
    if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED || status == InvoiceStatus.SENT) {
        throw new IllegalStateException("Cannot remove line items from " + status + " invoice");
    }
    // ...
}
```

**Note:** As per user instructions, I did NOT change the domain code. The tests are correctly expecting this behavior, but the domain doesn't enforce it yet.

---

## 🔍 Remaining Test Issues

### 1. testPartialPaymentDoesNotChangeToPaid
**Error:** Expected balance $600.00 but got $200.00  
**Location:** `InvoiceStateTransitionTest.testPartialPaymentDoesNotChangeToPaid`  
**Possible Cause:** Payment might be recorded twice, or calculation error  
**Status:** Needs investigation

### 2. testValidTransition_SentToPaid
**Error:** "Can only record payment for SENT or OVERDUE invoices. Current status: PAID"  
**Location:** `InvoiceStateTransitionTest.testValidTransition_SentToPaid`  
**Possible Cause:** Invoice becomes PAID after first payment, then test tries to record another payment  
**Status:** May need invoice reload after payment

---

## Summary

**Fixed:**
- ✅ 10 tests - Unique invoice numbers
- ✅ 5 tests - Line item update logic (add before remove)
- ✅ 1 test - Payment recording logic (reload invoice)

**Needs Domain Code Change:**
- ⚠️ 3 tests - SENT invoices should not allow line item additions/removals

**Needs Investigation:**
- 🔍 2 tests - Payment balance calculations

**Expected Pass Rate After Fixes:** ~90% (55/61 tests)

---

## Next Steps

1. ✅ **Invoice Numbers:** Fixed - tests should pass
2. ✅ **Line Item Updates:** Fixed - tests should pass  
3. ✅ **Payment Recording:** Fixed - test should pass
4. ⏭️ **Domain Rule:** Need to update `Invoice.addLineItem()` and `Invoice.removeLineItem()` to prevent SENT invoice edits
5. ⏭️ **Re-run Tests:** Execute test suite to verify fixes

