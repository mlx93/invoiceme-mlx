import { useState, useEffect, useCallback } from 'react';
import apiClient, { getErrorMessage, Page } from '@/lib/api';
import {
  CustomerResponse,
  CustomerDetailResponse,
  CreateCustomerRequest,
  UpdateCustomerRequest,
} from '@/types/customer';

interface UseCustomersOptions {
  page?: number;
  size?: number;
  status?: string;
  customerType?: string;
  search?: string;
}

export function useCustomers(options: UseCustomersOptions = {}) {
  const [customers, setCustomers] = useState<CustomerResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState<Omit<Page<CustomerResponse>, 'content'>>({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  const fetchCustomers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      if (options.page !== undefined) params.append('page', options.page.toString());
      if (options.size !== undefined) params.append('size', options.size.toString());
      if (options.status) params.append('status', options.status);
      if (options.customerType) params.append('customerType', options.customerType);
      if (options.search) params.append('search', options.search);

      const response = await apiClient.get<Page<CustomerResponse>>(`/customers?${params.toString()}`);
      setCustomers(response.data.content);
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
  }, [options.page, options.size, options.status, options.customerType, options.search]);

  useEffect(() => {
    fetchCustomers();
  }, [fetchCustomers]);

  return { customers, loading, error, pagination, refetch: fetchCustomers };
}

export function useCustomer(id: string | null) {
  const [customer, setCustomer] = useState<CustomerDetailResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    const fetchCustomer = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<CustomerDetailResponse>(`/customers/${id}`);
        setCustomer(response.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchCustomer();
  }, [id]);

  return { customer, loading, error };
}

export function useCreateCustomer() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createCustomer = async (data: CreateCustomerRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.post<CustomerResponse>('/customers', data);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { createCustomer, loading, error };
}

export function useUpdateCustomer() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const updateCustomer = async (id: string, data: UpdateCustomerRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.put<CustomerResponse>(`/customers/${id}`, data);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { updateCustomer, loading, error };
}

export function useDeleteCustomer() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const deleteCustomer = async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      await apiClient.delete(`/customers/${id}`);
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { deleteCustomer, loading, error };
}

