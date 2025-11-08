import { useState, useEffect, useCallback } from 'react';
import apiClient, { getErrorMessage, Page } from '@/lib/api';
import { RefundResponse, IssueRefundRequest } from '@/types/refund';

interface UseRefundsOptions {
  page?: number;
  size?: number;
  invoiceId?: string;
  customerId?: string;
}

export function useRefunds(options: UseRefundsOptions = {}) {
  const [refunds, setRefunds] = useState<RefundResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState<Omit<Page<RefundResponse>, 'content'>>({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  const fetchRefunds = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      if (options.page !== undefined) params.append('page', options.page.toString());
      if (options.size !== undefined) params.append('size', options.size.toString());
      if (options.invoiceId) params.append('invoiceId', options.invoiceId);
      if (options.customerId) params.append('customerId', options.customerId);

      const response = await apiClient.get<Page<RefundResponse>>(`/refunds?${params.toString()}`);
      setRefunds(response.data.content);
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
    fetchRefunds();
  }, [fetchRefunds]);

  return { refunds, loading, error, pagination, refetch: fetchRefunds };
}

export function useIssueRefund() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const issueRefund = async (data: IssueRefundRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.post<RefundResponse>('/refunds', data);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { issueRefund, loading, error };
}

