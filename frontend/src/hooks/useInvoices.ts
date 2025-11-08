import { useState, useEffect, useCallback } from 'react';
import apiClient, { getErrorMessage, Page } from '@/lib/api';
import {
  InvoiceResponse,
  InvoiceDetailResponse,
  CreateInvoiceRequest,
  UpdateInvoiceRequest,
} from '@/types/invoice';

interface UseInvoicesOptions {
  page?: number;
  size?: number;
  status?: string;
  customerId?: string;
  search?: string;
}

export function useInvoices(options: UseInvoicesOptions = {}) {
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState<Omit<Page<InvoiceResponse>, 'content'>>({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  const fetchInvoices = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      if (options.page !== undefined) params.append('page', options.page.toString());
      if (options.size !== undefined) params.append('size', options.size.toString());
      if (options.status) params.append('status', options.status);
      if (options.customerId) params.append('customerId', options.customerId);
      if (options.search) params.append('search', options.search);

      const response = await apiClient.get<Page<InvoiceResponse>>(`/invoices?${params.toString()}`);
      setInvoices(response.data.content);
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
  }, [options.page, options.size, options.status, options.customerId, options.search]);

  useEffect(() => {
    fetchInvoices();
  }, [fetchInvoices]);

  return { invoices, loading, error, pagination, refetch: fetchInvoices };
}

export function useInvoice(id: string | null) {
  const [invoice, setInvoice] = useState<InvoiceDetailResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    const fetchInvoice = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<InvoiceDetailResponse>(`/invoices/${id}`);
        setInvoice(response.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchInvoice();
  }, [id]);

  return { invoice, loading, error, refetch: () => id && fetchInvoice() };
}

export function useCreateInvoice() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createInvoice = async (data: CreateInvoiceRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.post<InvoiceResponse>('/invoices', data);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { createInvoice, loading, error };
}

export function useUpdateInvoice() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const updateInvoice = async (id: string, data: UpdateInvoiceRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.put<InvoiceResponse>(`/invoices/${id}`, data);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { updateInvoice, loading, error };
}

export function useMarkInvoiceAsSent() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const markAsSent = async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.patch<InvoiceResponse>(`/invoices/${id}/mark-as-sent`);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { markAsSent, loading, error };
}

export function useCancelInvoice() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const cancelInvoice = async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      await apiClient.delete(`/invoices/${id}`);
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { cancelInvoice, loading, error };
}

