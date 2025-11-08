import { Money } from './common';

export interface IssueRefundRequest {
  invoiceId: string;
  amount: number;
  reason: string;
  applyAsCredit?: boolean;
}

export interface RefundResponse {
  id: string;
  invoiceId: string;
  invoiceNumber: string;
  customerId: string;
  amount: Money;
  reason: string;
  applyAsCredit: boolean;
  createdAt: string;
}

