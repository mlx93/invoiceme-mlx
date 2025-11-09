import { Money, DiscountType, PaymentTerms, InvoiceStatus } from './common';

export interface LineItemDTO {
  description: string;
  quantity: number;
  unitPrice: number;
  discountType: DiscountType;
  discountValue?: number;
  taxRate: number;
}

export interface CreateInvoiceRequest {
  customerId: string;
  issueDate: string;
  dueDate?: string;
  paymentTerms: PaymentTerms;
  lineItems: LineItemDTO[];
  notes?: string;
}

export interface UpdateInvoiceRequest {
  issueDate?: string;
  dueDate?: string;
  paymentTerms?: PaymentTerms;
  lineItems?: LineItemDTO[];
  notes?: string;
  version: number; // Required for optimistic locking
}

export interface LineItemResponse {
  id: string;
  description: string;
  quantity: number;
  unitPrice: Money;
  discountType: DiscountType;
  discountValue: Money;
  taxRate: number;
  lineTotal: Money;
  sortOrder: number;
}

export interface PaymentSummary {
  id: string;
  amount: Money;
  paymentMethod: string;
  paymentDate: string;
  status: string;
}

export interface InvoiceResponse {
  id: string;
  invoiceNumber: string;
  customerId: string;
  customerName: string;
  issueDate: string;
  dueDate: string;
  status: InvoiceStatus;
  totalAmount: Money;
  amountPaid: Money;
  balanceDue: Money;
  createdAt: string;
}

export interface InvoiceDetailResponse extends InvoiceResponse {
  paymentTerms: PaymentTerms;
  lineItems: LineItemResponse[];
  subtotal: Money;
  taxAmount: Money;
  discountAmount: Money;
  notes?: string;
  sentDate?: string;
  paidDate?: string;
  payments: PaymentSummary[];
  pdfUrl: string;
  version: number; // For optimistic locking
}

