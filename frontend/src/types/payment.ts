import { Money, PaymentMethod, PaymentStatus } from './common';

export interface RecordPaymentRequest {
  invoiceId: string;
  amount: number;
  paymentMethod: PaymentMethod;
  paymentDate: string;
  paymentReference?: string;
  notes?: string;
}

export interface PaymentResponse {
  id: string;
  invoiceId: string;
  invoiceNumber: string;
  customerId: string;
  customerName: string;
  amount: Money;
  paymentMethod: PaymentMethod;
  paymentDate: string;
  paymentReference?: string;
  status: PaymentStatus;
  createdAt: string;
}

export interface PaymentDetailResponse extends PaymentResponse {
  notes?: string;
  createdByUserId: string;
  createdByName: string;
}

