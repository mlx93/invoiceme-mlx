import { Money } from './common';

export interface DashboardMetricsResponse {
  totalRevenueMTD: Money;
  outstandingInvoicesCount: number;
  outstandingInvoicesAmount: Money;
  overdueInvoicesCount: number;
  overdueInvoicesAmount: Money;
  activeCustomers: number;
}

export interface RevenueTrendData {
  month: string;
  revenue: Money;
}

export interface RevenueTrendResponse {
  data: RevenueTrendData[];
}

export interface InvoiceStatusData {
  status: string;
  count: number;
  amount: Money;
}

export interface InvoiceStatusResponse {
  data: InvoiceStatusData[];
}

export interface AgingReportData {
  bucket: '0-30' | '31-60' | '61-90' | '90+';
  count: number;
  amount: Money;
}

export interface AgingReportResponse {
  data: AgingReportData[];
}

