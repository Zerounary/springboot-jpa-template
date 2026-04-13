import { http, request } from './http'
import type { ApiResponse, IPage } from './types'

export type PatientDto = {
  id: number
  userId: string
  accountId?: number
  organizationId?: number
  gender: number
  age: number
  birthDate: string
  phone: string
  medicalInstitution: string
  nation?: string
  createdAt?: string
  updatedAt?: string
}

export type PatientCreateRequest = {
  userId: string
  accountId?: number
  organizationId?: number
  gender: number
  age: number
  birthDate: string
  phone: string
  medicalInstitution?: string
  nation?: string
}

export type PatientUpdateRequest = {
  gender: number
  age: number
  birthDate: string
  phone?: string
  organizationId?: number
  medicalInstitution?: string
  nation?: string
}

export function patientPageApi(params: { page: number; size: number; keyword?: string | null }) {
  return request<IPage<PatientDto>>(
    http.get('/api/patients', {
      params: {
        page: params.page,
        size: params.size,
        keyword: params.keyword || undefined,
      },
    }),
  )
}

export function patientDetailApi(id: number) {
  return request<PatientDto>(http.get(`/api/patients/${id}`))
}

export function patientMeApi() {
  return request<PatientDto>(http.get<ApiResponse<PatientDto>>('/api/patients/me'))
}

export function patientCreateApi(req: PatientCreateRequest) {
  return request<PatientDto>(http.post('/api/patients', req))
}

export function patientUpdateMeApi(req: PatientUpdateRequest) {
  return request<PatientDto>(http.put<ApiResponse<PatientDto>>('/api/patients/me', req))
}

export function patientUpdateApi(id: number, req: PatientUpdateRequest) {
  return request<PatientDto>(http.put(`/api/patients/${id}`, req))
}

export function patientDeleteApi(id: number) {
  return request<void>(http.delete(`/api/patients/${id}`))
}
