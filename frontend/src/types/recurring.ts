import { Money, RecurringFrequency, RecurringStatus, PaymentTerms, DiscountType } from './common';

export interface TemplateLineItemDTO {
  description: string;
  quantity: number;
  unitPrice: number;
  discountType: DiscountType;
  discountValue?: number;
  taxRate: number;
}

export interface CreateTemplateRequest {
  customerId: string;
  templateName: string;
  frequency: RecurringFrequency;
  startDate: string;
  endDate?: string;
  paymentTerms: PaymentTerms;
  autoSend: boolean;
  lineItems: TemplateLineItemDTO[];
}

export interface TemplateResponse {
  id: string;
  customerId: string;
  customerName: string;
  templateName: string;
  frequency: RecurringFrequency;
  startDate: string;
  endDate?: string;
  nextInvoiceDate?: string;
  status: RecurringStatus;
  autoSend: boolean;
  createdAt: string;
}

export interface TemplateDetailResponse extends TemplateResponse {
  paymentTerms: PaymentTerms;
  lineItems: TemplateLineItemDTO[];
}

