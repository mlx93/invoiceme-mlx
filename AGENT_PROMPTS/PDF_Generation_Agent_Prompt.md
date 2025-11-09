# PDF Invoice Generation Agent Prompt

**Date**: 2025-01-27  
**Agent Type**: Backend Feature Implementation Agent  
**Priority**: Medium  
**Estimated Effort**: 3-4 hours

---

## Context & Background

The InvoiceMe application currently has PDF generation stubbed (`pdfUrl = null` in invoice responses). We need to implement actual PDF invoice generation to complete this extended feature. The PDF should be generated on-demand when requested, follow a professional invoice template, and optionally cache in AWS S3 for performance.

**Current State**:
- Invoice responses include `pdfUrl` field (currently `null`)
- Frontend has PDF download button on invoice detail page
- No PDF generation service exists
- AWS S3 bucket configured (`invoiceme-pdfs-mlx`)

**Requirements**:
- Generate PDF invoices on-demand
- Professional invoice template with company branding
- Include all invoice details (customer, line items, totals, payment history)
- Optional S3 caching with signed URLs
- Support for invoice number format: `INV-YYYY-####`

---

## Implementation Goal

Implement a complete PDF invoice generation system that:
1. Generates professional PDF invoices using a Java PDF library (iText or Apache PDFBox)
2. Creates an API endpoint to generate/download PDFs
3. Optionally caches PDFs in AWS S3 with signed URLs (1-hour expiry)
4. Returns PDF URL in invoice detail responses
5. Handles errors gracefully (return null URL if generation fails)

---

## Execution Instructions

### Phase 1: Add PDF Generation Library

**Add Dependency** (`backend/pom.xml`):
```xml
<!-- iText 7 for PDF generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>8.0.2</version>
    <type>pom</type>
</dependency>
```

**Or Apache PDFBox** (alternative):
```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>
```

**Recommendation**: Use iText 7 (more modern, better API, professional output)

---

### Phase 2: Create PDF Service

**Create**: `backend/src/main/java/com/invoiceme/infrastructure/pdf/InvoicePdfService.java`

**Responsibilities**:
- Generate PDF from Invoice aggregate
- Format invoice with professional template
- Include company branding/logo (optional)
- Handle errors gracefully

**Method Signature**:
```java
public byte[] generateInvoicePdf(Invoice invoice, Customer customer) throws PdfGenerationException
```

**PDF Template Should Include**:
- **Header**: Company name, address, contact info
- **Invoice Details**: Invoice number, issue date, due date, payment terms
- **Customer Info**: Customer name, address, contact
- **Line Items Table**: Description, quantity, unit price, discount, tax, line total
- **Summary**: Subtotal, discount total, tax total, **Total Amount**
- **Payment History**: List of payments (if any)
- **Balance Due**: Outstanding balance
- **Footer**: Terms and conditions, payment instructions

---

### Phase 3: Create PDF Controller Endpoint

**Add to**: `backend/src/main/java/com/invoiceme/invoices/InvoiceController.java`

**New Endpoint**:
```java
@GetMapping("/{id}/pdf")
@PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT', 'SALES') or (hasRole('CUSTOMER') and @invoiceService.isOwnInvoice(#id, authentication.name))")
public ResponseEntity<Resource> downloadInvoicePdf(@PathVariable UUID id) {
    // Load invoice and customer
    // Generate PDF
    // Return PDF as ResponseEntity<Resource>
}
```

**Response**:
- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename="INV-2025-0001.pdf"`
- Return PDF bytes as `ByteArrayResource`

---

### Phase 4: Optional S3 Caching

**Create**: `backend/src/main/java/com/invoiceme/infrastructure/pdf/S3PdfCacheService.java`

**Responsibilities**:
- Check if PDF exists in S3 for invoice ID
- Upload PDF to S3 if not cached
- Generate signed URL (1-hour expiry)
- Return cached URL or generate new PDF

**S3 Key Format**: `invoices/{invoiceId}/invoice.pdf`

**Signed URL Expiry**: 1 hour (configurable)

**Integration**:
- Use existing AWS S3 configuration
- Use `AWS_S3_BUCKET_NAME` environment variable
- Handle S3 errors gracefully (fallback to direct generation)

---

### Phase 5: Update Invoice Detail Response

**Update**: `backend/src/main/java/com/invoiceme/invoices/getinvoice/GetInvoiceHandler.java`

**Changes**:
- Generate PDF URL when loading invoice detail
- Use S3 signed URL if caching enabled
- Use direct download endpoint if no caching
- Return `null` if PDF generation fails (don't break invoice detail)

**URL Format**:
- With S3: `https://s3.amazonaws.com/bucket/invoices/{id}/invoice.pdf?signature=...`
- Without S3: `/api/v1/invoices/{id}/pdf`

---

### Phase 6: Error Handling

**Create**: `backend/src/main/java/com/invoiceme/infrastructure/pdf/PdfGenerationException.java`

**Error Handling Strategy**:
- Catch PDF generation errors
- Log errors (don't expose to user)
- Return `null` for `pdfUrl` in responses
- Don't break invoice detail if PDF fails

---

## PDF Template Design

### Invoice Layout

```
┌─────────────────────────────────────────────────────────┐
│  [Company Logo]    INVOICE                              │
│  FloodShield Restoration                                │
│  123 Main St, Austin, TX 78701                          │
│  Phone: (512) 555-0100                                  │
│                                                          │
│  Invoice #: INV-2025-0001                                │
│  Issue Date: January 15, 2025                           │
│  Due Date: February 14, 2025                            │
│  Payment Terms: Net 30                                   │
│                                                          │
│  Bill To:                                               │
│  Riverside Apartments LLC                                │
│  John Doe                                                │
│  john@riversideapts.com                                 │
│  456 Oak Ave, Austin, TX 78702                          │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Description          Qty  Unit Price  Discount  Tax    │
│  ─────────────────────────────────────────────────────  │
│  Emergency Response   1    $450.00     -        $37.13  │
│  Water Extraction     6    $125.00     -        $61.88  │
│  Equipment Setup      1    $2,100.00   -        $173.25 │
│  Anti-microbial       1    $950.00     -        $78.38  │
│                                                          │
│  Subtotal:                                    $4,250.00 │
│  Tax (8.25%):                                    $350.64│
│  ─────────────────────────────────────────────────────  │
│  Total Amount:                                 $4,600.64│
│  Amount Paid:                                   $4,600.64│
│  Balance Due:                                         $0 │
└─────────────────────────────────────────────────────────┘

Payment History:
- January 16, 2025: $4,600.64 (Credit Card) - COMPLETED

Thank you for your business!
```

---

## Implementation Details

### PDF Library Choice: iText 7

**Why iText 7**:
- Professional PDF generation
- Good Java API
- Supports tables, fonts, images
- Active maintenance
- Free for open source projects

**Key Classes**:
- `PdfDocument` - Main PDF document
- `Document` - High-level document API
- `Table` - For line items table
- `Paragraph` - For text blocks

### Example Code Structure

```java
@Service
@RequiredArgsConstructor
public class InvoicePdfService {
    
    public byte[] generateInvoicePdf(Invoice invoice, Customer customer) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        // Add header
        addHeader(document);
        
        // Add invoice details
        addInvoiceDetails(document, invoice);
        
        // Add customer info
        addCustomerInfo(document, customer);
        
        // Add line items table
        addLineItemsTable(document, invoice);
        
        // Add summary
        addSummary(document, invoice);
        
        // Add payment history
        addPaymentHistory(document, invoice);
        
        // Add footer
        addFooter(document);
        
        document.close();
        return baos.toByteArray();
    }
}
```

---

## Testing Requirements

### Unit Tests
- [ ] Test PDF generation with sample invoice
- [ ] Test PDF contains all required fields
- [ ] Test PDF formatting (tables, fonts, layout)
- [ ] Test error handling (invalid invoice data)

### Integration Tests
- [ ] Test PDF download endpoint (`GET /api/v1/invoices/{id}/pdf`)
- [ ] Test PDF URL in invoice detail response
- [ ] Test RBAC enforcement (only authorized users can download)
- [ ] Test S3 caching (if implemented)

### Manual Testing
- [ ] Download PDF from invoice detail page
- [ ] Verify PDF opens correctly
- [ ] Verify PDF contains all invoice information
- [ ] Verify PDF formatting is professional

---

## Configuration

### Environment Variables

**For S3 Caching** (optional):
```bash
AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
AWS_REGION=us-east-1
PDF_CACHE_ENABLED=true
PDF_SIGNED_URL_EXPIRY_HOURS=1
```

**For Direct Generation** (no caching):
```bash
PDF_CACHE_ENABLED=false
```

---

## Success Criteria

- ✅ PDF generation service implemented
- ✅ PDF download endpoint created (`GET /api/v1/invoices/{id}/pdf`)
- ✅ PDF URL returned in invoice detail responses
- ✅ Professional invoice template with all required fields
- ✅ Error handling (graceful failures, don't break invoice detail)
- ✅ RBAC enforcement (authorized users only)
- ✅ Optional S3 caching with signed URLs
- ✅ Unit tests written
- ✅ Integration tests written
- ✅ Manual testing completed

---

## Deliverables

1. **PDF Service**: `InvoicePdfService.java` with professional template
2. **PDF Endpoint**: Added to `InvoiceController.java`
3. **S3 Cache Service**: `S3PdfCacheService.java` (optional)
4. **Exception Class**: `PdfGenerationException.java`
5. **Tests**: Unit and integration tests
6. **Documentation**: Update API docs with PDF endpoint

---

## Reference Documents

- **InvoiceMe.md**: Core requirements (invoice structure)
- **PRD_1_Business_Reqs.md**: Invoice format requirements
- **Backend M2 Complete**: Current invoice structure
- **AWS S3 Setup**: S3 bucket configuration

---

## Notes

- **Performance**: PDF generation is CPU-intensive. Consider async generation for large invoices.
- **Caching**: S3 caching recommended for production (reduces generation load).
- **Error Handling**: PDF failures should not break invoice detail page (return null URL).
- **Branding**: Logo/images optional for MVP, can be added later.

---

**Status**: ✅ **READY FOR EXECUTION** - Clear requirements, well-defined scope

