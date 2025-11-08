import { useState } from 'react';
import { useRouter } from 'next/navigation';
import apiClient, { getErrorMessage } from '@/lib/api';
import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '@/types/user';
import { useAuth as useAuthContext } from '@/contexts/AuthContext';

export function useLogin() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { login } = useAuthContext();
  const router = useRouter();

  const loginUser = async (credentials: LoginRequest) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await apiClient.post<LoginResponse>('/auth/login', credentials);
      login(response.data.token, response.data.user);
      router.push('/dashboard');
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  };

  return { loginUser, isLoading, error };
}

export function useRegister() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const registerUser = async (data: RegisterRequest) => {
    setIsLoading(true);
    setError(null);
    try {
      await apiClient.post<RegisterResponse>('/auth/register', data);
      router.push('/login?registered=true');
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  };

  return { registerUser, isLoading, error };
}

export function useLogout() {
  const { logout } = useAuthContext();
  return logout;
}

