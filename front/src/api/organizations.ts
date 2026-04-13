import { http, request } from './http'
import type { ApiResponse } from './types'

export type OrganizationDto = {
  id: number
  orgCode?: string
  orgName: string
  createdAt?: string
  updatedAt?: string
}

export type OrganizationCreateRequest = {
  orgCode?: string
  orgName: string
}

export type OrganizationUpdateRequest = {
  orgCode?: string
  orgName: string
}

export function organizationListApi(params: { keyword?: string | null }) {
  return request<OrganizationDto[]>(
    http.get<ApiResponse<OrganizationDto[]>>('/api/organizations', {
      params: {
        keyword: params.keyword || undefined,
      },
    }),
  )
}

export function organizationDetailApi(id: number) {
  return request<OrganizationDto>(http.get<ApiResponse<OrganizationDto>>(`/api/organizations/${id}`))
}

export function organizationCreateApi(req: OrganizationCreateRequest) {
  return request<OrganizationDto>(http.post<ApiResponse<OrganizationDto>>('/api/organizations', req))
}

export function organizationUpdateApi(id: number, req: OrganizationUpdateRequest) {
  return request<OrganizationDto>(http.put<ApiResponse<OrganizationDto>>(`/api/organizations/${id}`, req))
}

export function organizationDeleteApi(id: number) {
  return request<void>(http.delete<ApiResponse<void>>(`/api/organizations/${id}`))
}
