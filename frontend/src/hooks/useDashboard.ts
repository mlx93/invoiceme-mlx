import { useState, useEffect } from 'react';
import apiClient, { getErrorMessage } from '@/lib/api';
import {
  DashboardMetricsResponse,
  RevenueTrendResponse,
  InvoiceStatusResponse,
  AgingReportResponse,
} from '@/types/dashboard';

export function useDashboardMetrics() {
  const [metrics, setMetrics] = useState<DashboardMetricsResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchMetrics = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<DashboardMetricsResponse>('/dashboard/metrics');
        setMetrics(response.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchMetrics();
  }, []);

  return { metrics, loading, error };
}

export function useRevenueTrend(months: number = 12) {
  const [data, setData] = useState<RevenueTrendResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTrend = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<RevenueTrendResponse>(
          `/dashboard/revenue-trend?months=${months}`
        );
        setData(response.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchTrend();
  }, [months]);

  return { data, loading, error };
}

export function useInvoiceStatus() {
  const [data, setData] = useState<InvoiceStatusResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStatus = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<InvoiceStatusResponse>('/dashboard/invoice-status');
        setData(response.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchStatus();
  }, []);

  return { data, loading, error };
}

export function useAgingReport() {
  const [data, setData] = useState<AgingReportResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchAging = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<AgingReportResponse>('/dashboard/aging-report');
        setData(response.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchAging();
  }, []);

  return { data, loading, error };
}

