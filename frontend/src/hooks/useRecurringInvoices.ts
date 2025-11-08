import { useState, useEffect, useCallback } from 'react';
import apiClient, { getErrorMessage, Page } from '@/lib/api';
import {
  TemplateResponse,
  TemplateDetailResponse,
  CreateTemplateRequest,
} from '@/types/recurring';

interface UseRecurringInvoicesOptions {
  page?: number;
  size?: number;
  customerId?: string;
  status?: string;
}

export function useRecurringInvoices(options: UseRecurringInvoicesOptions = {}) {
  const [templates, setTemplates] = useState<TemplateResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState<Omit<Page<TemplateResponse>, 'content'>>({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  const fetchTemplates = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      if (options.page !== undefined) params.append('page', options.page.toString());
      if (options.size !== undefined) params.append('size', options.size.toString());
      if (options.customerId) params.append('customerId', options.customerId);
      if (options.status) params.append('status', options.status);

      const response = await apiClient.get<Page<TemplateResponse>>(
        `/recurring-invoices?${params.toString()}`
      );
      setTemplates(response.data.content);
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
  }, [options.page, options.size, options.customerId, options.status]);

  useEffect(() => {
    fetchTemplates();
  }, [fetchTemplates]);

  return { templates, loading, error, pagination, refetch: fetchTemplates };
}

export function useRecurringInvoice(id: string | null) {
  const [template, setTemplate] = useState<TemplateDetailResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    const fetchTemplate = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<TemplateDetailResponse>(`/recurring-invoices/${id}`);
        setTemplate(response.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchTemplate();
  }, [id]);

  return { template, loading, error };
}

export function useCreateRecurringInvoice() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createTemplate = async (data: CreateTemplateRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.post<TemplateResponse>('/recurring-invoices', data);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { createTemplate, loading, error };
}

export function usePauseTemplate() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const pauseTemplate = async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.patch<TemplateResponse>(`/recurring-invoices/${id}/pause`);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { pauseTemplate, loading, error };
}

export function useResumeTemplate() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const resumeTemplate = async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.patch<TemplateResponse>(`/recurring-invoices/${id}/resume`);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { resumeTemplate, loading, error };
}

export function useCompleteTemplate() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const completeTemplate = async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.patch<TemplateResponse>(
        `/recurring-invoices/${id}/complete`
      );
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { completeTemplate, loading, error };
}

