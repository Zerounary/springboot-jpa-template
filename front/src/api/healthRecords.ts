import { http, request } from './http'
import type { ApiResponse, IPage } from './types'

export type HealthRecordDto = {
  id: number
  patientId: number
  familyHypertension: number
  pastHypertension: number
  comorbidity: string
  drugHistory: string
  treatmentRecord?: string
  allergyHistory?: string
  createdAt?: string
  updatedAt?: string
}

export type HealthRecordCreateRequest = {
  patientId: number
  familyHypertension: number
  pastHypertension: number
  comorbidity: string
  drugHistory: string
  treatmentRecord?: string
  allergyHistory?: string
}

export type HealthRecordUpdateRequest = {
  familyHypertension: number
  pastHypertension: number
  comorbidity: string
  drugHistory: string
  treatmentRecord?: string
  allergyHistory?: string
}

export function healthRecordPageApi(params: {
  page: number
  size: number
  patientId?: number | null
  keyword?: string | null
  startTime?: string | null
  endTime?: string | null
}) {
  return request<IPage<HealthRecordDto>>(
    http.get<ApiResponse<IPage<HealthRecordDto>>>('/api/health-records', {
      params: {
        page: params.page,
        size: params.size,
        patientId: params.patientId ?? undefined,
        keyword: params.keyword || undefined,
        startTime: params.startTime ?? undefined,
        endTime: params.endTime ?? undefined,
      },
    }),
  )
}

export function healthRecordDetailApi(id: number) {
  return request<HealthRecordDto>(http.get<ApiResponse<HealthRecordDto>>(`/api/health-records/${id}`))
}

export function healthRecordCreateApi(req: HealthRecordCreateRequest) {
  return request<HealthRecordDto>(http.post<ApiResponse<HealthRecordDto>>('/api/health-records', req))
}

export function healthRecordUpdateApi(id: number, req: HealthRecordUpdateRequest) {
  return request<HealthRecordDto>(http.put<ApiResponse<HealthRecordDto>>(`/api/health-records/${id}`, req))
}

export function healthRecordDeleteApi(id: number) {
  return request<void>(http.delete<ApiResponse<void>>(`/api/health-records/${id}`))
}
