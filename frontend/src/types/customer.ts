import { Address, CustomerType, CustomerStatus, Money } from './common';

export interface CreateCustomerRequest {
  companyName: string;
  contactName?: string;
  email: string;
  phone?: string;
  address?: Address;
  customerType: CustomerType;
}

export interface UpdateCustomerRequest {
  companyName?: string;
  contactName?: string;
  phone?: string;
  address?: Address;
  customerType?: CustomerType;
  status?: CustomerStatus;
}

export interface CustomerResponse {
  id: string;
  companyName: string;
  contactName?: string;
  email: string;
  phone?: string;
  address?: Address;
  customerType: CustomerType;
  creditBalance: Money;
  status: CustomerStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerDetailResponse extends CustomerResponse {
  outstandingBalance: Money;
  totalInvoices: number;
  unpaidInvoices: number;
}

