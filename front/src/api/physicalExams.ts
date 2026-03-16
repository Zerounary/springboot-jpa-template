import { http, request } from './http'
import type { ApiResponse, IPage } from './types'

export type PhysicalExamDto = {
  id: number
  patientId: number
  examTime: string
  systolicBp: number
  diastolicBp: number
  bmi: number
  cholesterol: number
  fastingBloodSugar: number
  height: number
  weight: number
  heartRate?: number
  liverFunction?: number
  createdAt?: string
  updatedAt?: string
}

export type PhysicalExamCreateRequest = {
  patientId: number
  examTime: string
  systolicBp: number
  diastolicBp: number
  bmi: number
  cholesterol: number
  fastingBloodSugar: number
  height: number
  weight: number
  heartRate?: number
  liverFunction?: number
}

export type PhysicalExamUpdateRequest = {
  examTime: string
  systolicBp: number
  diastolicBp: number
  bmi: number
  cholesterol: number
  fastingBloodSugar: number
  height: number
  weight: number
  heartRate?: number
  liverFunction?: number
}

export function physicalExamPageApi(params: { page: number; size: number; patientId?: number | null }) {
  return request<IPage<PhysicalExamDto>>(
    http.get<ApiResponse<IPage<PhysicalExamDto>>>('/api/physical-exams', {
      params: {
        page: params.page,
        size: params.size,
        patientId: params.patientId ?? undefined,
      },
    }),
  )
}

export function physicalExamDetailApi(id: number) {
  return request<PhysicalExamDto>(http.get<ApiResponse<PhysicalExamDto>>(`/api/physical-exams/${id}`))
}

export function physicalExamCreateApi(req: PhysicalExamCreateRequest) {
  return request<PhysicalExamDto>(http.post<ApiResponse<PhysicalExamDto>>('/api/physical-exams', req))
}

export function physicalExamUpdateApi(id: number, req: PhysicalExamUpdateRequest) {
  return request<PhysicalExamDto>(http.put<ApiResponse<PhysicalExamDto>>(`/api/physical-exams/${id}`, req))
}

export function physicalExamDeleteApi(id: number) {
  return request<void>(http.delete<ApiResponse<void>>(`/api/physical-exams/${id}`))
}
