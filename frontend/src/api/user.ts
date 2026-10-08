import request from '@/utils/request';
import type { ApiResponse, LoginForm, LoginPayload, RegisterForm, UserProfile } from '@/types';

export function login(data: LoginForm): Promise<ApiResponse<LoginPayload>> {
  return request.post('/api/users/login', data);
}

export function register(data: RegisterForm): Promise<ApiResponse<UserProfile>> {
  return request.post('/api/users/register', data);
}

export function getProfile(): Promise<ApiResponse<UserProfile>> {
  return request.get('/api/users/profile');
}
