import { http, request } from './http'
import type { ApiResponse, IPage } from './types'

export type UserDto = {
  id: number
  username: string
  nickname?: string
  email?: string
  createdAt?: string
  updatedAt?: string
}

export type UserCreateRequest = {
  username: string
  password: string
  nickname?: string
  email?: string
}

export type UserUpdateRequest = {
  nickname?: string
  email?: string
  password?: string
}

export function userPageApi(params: { page: number; size: number; keyword?: string | null }) {
  return request<IPage<UserDto>>(
    http.get<ApiResponse<IPage<UserDto>>>('/api/users', {
      params: {
        page: params.page,
        size: params.size,
        keyword: params.keyword || undefined,
      },
    }),
  )
}

export function userDetailApi(id: number) {
  return request<UserDto>(http.get<ApiResponse<UserDto>>(`/api/users/${id}`))
}

export function userCreateApi(req: UserCreateRequest) {
  return request<UserDto>(http.post<ApiResponse<UserDto>>('/api/users', req))
}

export function userUpdateApi(id: number, req: UserUpdateRequest) {
  return request<UserDto>(http.put<ApiResponse<UserDto>>(`/api/users/${id}`, req))
}

export function userDeleteApi(id: number) {
  return request<void>(http.delete<ApiResponse<void>>(`/api/users/${id}`))
}
