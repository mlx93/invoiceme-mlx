import { useState, useEffect } from 'react';
import apiClient, { getErrorMessage } from '@/lib/api';
import { UserResponse, PendingUserListResponse } from '@/types/user';

export function usePendingUsers() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchPendingUsers = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<PendingUserListResponse>('/users/pending');
        setUsers(response.data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchPendingUsers();
  }, []);

  return { users, loading, error, refetch: fetchPendingUsers };
}

export function useApproveUser() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const approveUser = async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.patch<UserResponse>(`/users/${id}/approve`);
      return response.data;
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { approveUser, loading, error };
}

export function useRejectUser() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const rejectUser = async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      await apiClient.patch(`/users/${id}/reject`);
    } catch (err) {
      setError(getErrorMessage(err));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { rejectUser, loading, error };
}

