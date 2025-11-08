// Common types matching backend DTOs

export interface Money {
  amount: number;
  currency: string;
}

export interface Address {
  street?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
}

export type CustomerType = 'RESIDENTIAL' | 'COMMERCIAL' | 'INSURANCE';
export type CustomerStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
export type InvoiceStatus = 'DRAFT' | 'SENT' | 'PAID' | 'OVERDUE' | 'CANCELLED';
export type PaymentMethod = 'CREDIT_CARD' | 'ACH';
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
export type PaymentTerms = 'NET_30' | 'DUE_ON_RECEIPT' | 'CUSTOM';
export type DiscountType = 'NONE' | 'PERCENTAGE' | 'FIXED';
export type UserRole = 'SYSADMIN' | 'ACCOUNTANT' | 'SALES' | 'CUSTOMER';
export type UserStatus = 'PENDING' | 'ACTIVE' | 'INACTIVE' | 'LOCKED';
export type RecurringFrequency = 'MONTHLY' | 'QUARTERLY' | 'ANNUALLY';
export type RecurringStatus = 'ACTIVE' | 'PAUSED' | 'COMPLETED';

