import { http, request } from './http'
import type { ApiResponse, IPage } from './types'

export type HealthGuidanceDto = {
  id: number
  patientId: number
  doctorUserId: number
  predictionResultId?: number
  guidanceTitle: string
  guidanceContent: string
  guidanceLevel: number
  createdAt?: string
  updatedAt?: string
}

export type HealthGuidanceCreateRequest = {
  patientId: number
  predictionResultId?: number
  guidanceTitle: string
  guidanceContent: string
  guidanceLevel: number
}

export type HealthGuidanceUpdateRequest = {
  predictionResultId?: number
  guidanceTitle: string
  guidanceContent: string
  guidanceLevel: number
}

export function healthGuidancePageApi(params: { page: number; size: number; patientId?: number | null }) {
  return request<IPage<HealthGuidanceDto>>(
    http.get<ApiResponse<IPage<HealthGuidanceDto>>>('/api/health-guidances', {
      params: {
        page: params.page,
        size: params.size,
        patientId: params.patientId ?? undefined,
      },
    }),
  )
}

export function healthGuidanceMineApi(params: { page: number; size: number }) {
  return request<IPage<HealthGuidanceDto>>(
    http.get<ApiResponse<IPage<HealthGuidanceDto>>>('/api/health-guidances/mine', {
      params: {
        page: params.page,
        size: params.size,
      },
    }),
  )
}

export function healthGuidanceCreateApi(req: HealthGuidanceCreateRequest) {
  return request<HealthGuidanceDto>(http.post<ApiResponse<HealthGuidanceDto>>('/api/health-guidances', req))
}

export function healthGuidanceUpdateApi(id: number, req: HealthGuidanceUpdateRequest) {
  return request<HealthGuidanceDto>(http.put<ApiResponse<HealthGuidanceDto>>(`/api/health-guidances/${id}`, req))
}

export function healthGuidanceDeleteApi(id: number) {
  return request<void>(http.delete<ApiResponse<void>>(`/api/health-guidances/${id}`))
}
