import { useState, useEffect, useCallback } from 'react';
import apiClient, { getErrorMessage, Page } from '@/lib/api';
import { PaymentResponse, PaymentDetailResponse, RecordPaymentRequest } from '@/types/payment';

interface UsePaymentsOptions {
  page?: number;
  size?: number;
  invoiceId?: string;
  customerId?: string;
}

export function usePayments(options: UsePaymentsOptions = {}) {
  const [payments, setPayments] = useState<PaymentResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState<Omit<Page<PaymentResponse>, 'content'>>({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  const fetchPayments = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      if (options.page !== undefined) params.append('page', options.page.toString());
      if (options.size !== undefined) params.append('size', options.size.toString());
      if (options.invoiceId) params.append('invoiceId', options.invoiceId);
      if (options.customerId) params.append('customerId', options.customerId);

      const response = await apiClient.get<Page<PaymentResponse>>(`/payments?${params.toString()}`);
      setPayments(response.data.content);
      setPagination({
        page: response.data.page,
        size: response.data.size,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages,
        first: response.data.first,
        last: response.data.last,
      });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [options.page, options.size, options.invoiceId, options.customerId]);

  useEffect(() => {
    fetchPayments();
  }, [fetchPayments]);

  return { payments, loading, error, pagination, refetch: fetchPayments };
}

export function useRecordPayment() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const recordPayment = async (data: RecordPaymentRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.post<PaymentResponse>('/payments', data);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { recordPayment, loading, error };
}

