import { UserRole, UserStatus } from './common';

export type { UserRole, UserStatus };

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  role: UserRole;
}

export interface RegisterResponse {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  status: UserStatus;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: UserResponse;
}

export interface UserResponse {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  customerId?: string;
  status: UserStatus;
  createdAt: string;
}

export interface JwtPayload {
  sub: string;
  email: string;
  role: UserRole;
  customerId?: string;
  iat: number;
  exp: number;
}

export type PendingUserListResponse = UserResponse[];

