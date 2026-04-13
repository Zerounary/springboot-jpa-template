import { http, request } from './http'
import type { ApiResponse } from './types'

export type UserRole = 'PATIENT' | 'DOCTOR' | 'ADMIN'

export type LoginRequest = {
  username: string
  password: string
}

export type RegisterRequest = {
  username: string
  password: string
  nickname?: string
  role?: UserRole
}

export type UserDto = {
  id: number
  username: string
  role: UserRole
  nickname?: string
  email?: string
  createdAt?: string
  updatedAt?: string
}

export function loginApi(req: LoginRequest) {
  return request<string>(http.post<ApiResponse<string>>('/api/auth/login', req))
}

export function registerApi(req: RegisterRequest) {
  return request<UserDto>(http.post<ApiResponse<UserDto>>('/api/auth/register', req))
}

export function meApi() {
  return request<UserDto>(http.get<ApiResponse<UserDto>>('/api/auth/me'))
}
